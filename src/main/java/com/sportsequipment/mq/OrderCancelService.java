package com.sportsequipment.mq;

import com.sportsequipment.entity.Order;
import com.sportsequipment.entity.OrderItem;
import com.sportsequipment.exception.ResourceNotFoundException;
import com.sportsequipment.exception.UnauthorizedException;
import com.sportsequipment.mapper.OrderItemMapper;
import com.sportsequipment.mapper.OrderMapper;
import com.sportsequipment.service.OrderCancelTxService;
import com.sportsequipment.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 订单取消领域服务（重构为「锁在事务外」模式，与 OrderServiceImpl.createOrder 对齐）：
 * - 消费者收到延迟消息会调到这里；
 * - 手工取消订单的 Controller/Service 也可以复用；
 * - 统一做"状态前置判断+拿所有商品锁+调代理事务方法(库存归还+状态更新)+清缓存+幂等 Redis 记录"，
 *   保证"事务先提交 → 锁后释放"，避免锁释放后事务未提交的并发窗口。
 */
@Service
public class OrderCancelService {

    private static final Logger log = LoggerFactory.getLogger(OrderCancelService.class);

    private static final String ORDER_CANCEL_IDEMPOTENT_PREFIX = "mq:idempotent:order-cancel:";
    private static final String PRODUCT_LOCK_PREFIX = "lock:product:";
    private static final String PRODUCT_DETAIL_CACHE_PREFIX = "product:detail::";
    private static final String ORDER_LIST_USER_CACHE_PREFIX = "order:list:user:";
    private static final String ORDER_LIST_ADMIN_CACHE_PREFIX = "order:list:admin:*";

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final RedisUtil redisUtil;
    private final OrderCancelTxService orderCancelTxService;
    private final int idempotentTtlSeconds;

    public OrderCancelService(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                              RedisUtil redisUtil, OrderCancelTxService orderCancelTxService,
                              org.springframework.core.env.Environment env) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.redisUtil = redisUtil;
        this.orderCancelTxService = orderCancelTxService;
        String cfg = env.getProperty("sportsequipment.mq.idempotent-ttl-seconds", "86400");
        this.idempotentTtlSeconds = Integer.parseInt(cfg);
    }

    /**
     * 取消订单（归还库存+清理缓存）。
     * @param operatorRole 操作者角色：ADMIN / USER / SYSTEM（消费者）
     * @param operatorId 操作者用户ID（SYSTEM 可为 null）
     * @param skipPermissionCheck SYSTEM/ADMIN 可跳；普通 USER 要校验订单归属
     * @return true：这次真实取消成功；false：已取消/非 PENDING，幂等命中直接返回
     */
    public boolean cancelOrderAndRestoreStock(Long orderId, String operatorRole, Long operatorId, boolean skipPermissionCheck) {
        // 1) 幂等：已处理过的订单直接返回
        if (Boolean.TRUE.equals(redisUtil.hasKey(ORDER_CANCEL_IDEMPOTENT_PREFIX + orderId))) {
            log.info("[cancelOrder] 幂等命中，订单 {} 已处理，直接忽略", orderId);
            return false;
        }

        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }

        // 2) 权限
        if (!skipPermissionCheck && !"ADMIN".equals(operatorRole)) {
            if (operatorId == null || !operatorId.equals(order.getUserId())) {
                throw new UnauthorizedException("您无权操作该订单");
            }
        }

        // 3) 只能取消 PENDING
        if (!"PENDING".equals(order.getStatus())) {
            log.info("[cancelOrder] 订单 {} 当前状态 {} 非 PENDING，无需取消", orderId, order.getStatus());
            // 记录幂等，防止每次消息都进来重复判断 DB
            markIdempotent(orderId);
            return false;
        }

        // 4) 查订单项
        List<OrderItem> items = orderItemMapper.findByOrderId(orderId);

        // 5) 对 productId 去重 + 升序排序：所有请求按同一顺序拿锁，彻底避免 A 拿锁1等锁2、B 拿锁2等锁1 的死锁
        List<Long> sortedUniqueProductIds = items.stream()
                .map(OrderItem::getProductId)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        // 6) 依次拿所有商品的分布式锁（事务外，防死锁），用一个列表记录"成功拿到的锁 key"，finally 里倒序释放
        List<String> acquiredLockKeys = new ArrayList<>(sortedUniqueProductIds.size());
        try {
            for (Long productId : sortedUniqueProductIds) {
                String lockKey = PRODUCT_LOCK_PREFIX + productId;
                // waitTime=10s：允许排队等待；leaseTime=30s：显式过期，不开 watchdog，防止服务宕机锁永久持有
                boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
                if (!locked) {
                    throw new IllegalStateException("系统繁忙，请稍后再试（产品库存锁：product=" + productId + "）");
                }
                acquiredLockKeys.add(lockKey);
                log.debug("[cancelOrder] 拿到锁 productId={}, lockKey={}", productId, lockKey);
            }

            // 7) 锁已拿齐 → 通过代理调 OrderCancelTxService 开事务
            // ✅ 关键：锁在事务外，事务方法返回时 Spring 会先提交/回滚事务，控制权回到这里后才进 finally 释放锁
            // 顺序：拿锁 → 开事务 → 归还库存+改状态 → 提交事务 → 释放锁
            orderCancelTxService.restoreStockAndMarkCancelledInTx(order, items);

            // 8) 事务已提交 → 清缓存 + 幂等 SET（Redis 操作在事务外，避免回滚但幂等已 SET 导致漏归还）
            for (OrderItem item : items) {
                redisUtil.delete(PRODUCT_DETAIL_CACHE_PREFIX + item.getProductId());
            }
            redisUtil.deletePattern(ORDER_LIST_USER_CACHE_PREFIX + order.getUserId() + ":*");
            redisUtil.deletePattern(ORDER_LIST_ADMIN_CACHE_PREFIX);
            markIdempotent(orderId);

            log.info("[cancelOrder] 订单 {} 取消成功，归还 {} 个订单项库存，操作人={}", orderId, items.size(), operatorRole);
            return true;

        } finally {
            // 9) 事务提交/回滚完毕 → 释放所有锁（倒序释放，与拿锁对称）
            for (int i = acquiredLockKeys.size() - 1; i >= 0; i--) {
                String lockKey = acquiredLockKeys.get(i);
                try {
                    redisUtil.unlock(lockKey);
                } catch (Exception e) {
                    log.error("[cancelOrder] 释放锁失败 lockKey={}，依赖 30s leaseTime 兜底自动过期", lockKey, e);
                }
            }
        }
    }

    private void markIdempotent(Long orderId) {
        redisUtil.set(ORDER_CANCEL_IDEMPOTENT_PREFIX + orderId, "1", idempotentTtlSeconds, TimeUnit.SECONDS);
    }
}
