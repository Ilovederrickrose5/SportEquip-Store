package com.sportsequipment.mq;

import com.sportsequipment.exception.ResourceNotFoundException;
import com.sportsequipment.exception.UnauthorizedException;
import com.sportsequipment.mapper.OrderItemMapper;
import com.sportsequipment.mapper.OrderMapper;
import com.sportsequipment.mapper.ProductMapper;
import com.sportsequipment.entity.Order;
import com.sportsequipment.entity.OrderItem;
import com.sportsequipment.entity.Product;
import com.sportsequipment.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 订单取消领域服务：
 * - 消费者收到延迟消息会调到这里；
 * - 手工取消订单的 Controller/Service 也可以复用；
 * - 统一做"状态前置判断+库存归还+分布式锁+缓存清理+幂等 Redis 记录"，避免消息重试或并发重复取消。
 */
@Service
public class OrderCancelService {

    private static final Logger log = LoggerFactory.getLogger(OrderCancelService.class);

    private static final String ORDER_CANCEL_IDEMPOTENT_PREFIX = "mq:idempotent:order-cancel:";
    private static final String PRODUCT_LOCK_PREFIX = "lock:product:";

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final RedisUtil redisUtil;
    private final int idempotentTtlSeconds;

    public OrderCancelService(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                              ProductMapper productMapper, RedisUtil redisUtil,
                              org.springframework.core.env.Environment env) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
        this.redisUtil = redisUtil;
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
    @Transactional
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

        // 4) 归还每个订单项的库存（分布式锁 + 双重检查）
        List<OrderItem> items = orderItemMapper.findByOrderId(orderId);
        for (OrderItem item : items) {
            Long productId = item.getProductId();
            String lockKey = PRODUCT_LOCK_PREFIX + productId;
            try {
                boolean locked = redisUtil.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
                if (!locked) {
                    throw new IllegalStateException("系统繁忙，请稍后重试（产品库存锁：product=" + productId + "）");
                }
                Product product = productMapper.findById(productId);
                if (product == null) {
                    log.warn("[cancelOrder] 订单项 productId={} 已不存在，跳过库存归还", productId);
                    continue;
                }
                int qty = item.getQuantity() == null ? 0 : item.getQuantity();
                product.setStock(product.getStock() + qty);
                product.setUpdatedAt(java.time.LocalDateTime.now());
                productMapper.update(product);
                // 缓存一致性：删商品详情
                redisUtil.delete("product:detail::" + productId);
            } finally {
                redisUtil.unlock(lockKey);
            }
        }

        // 5) 订单状态 → CANCELLED
        order.setStatus("CANCELLED");
        order.setUpdatedAt(java.time.LocalDateTime.now());
        orderMapper.update(order);

        // 6) 清除订单列表缓存
        String userCachePrefix = "order:list:user:" + order.getUserId() + ":*";
        String adminCachePrefix = "order:list:admin:*";
        redisUtil.deletePattern(userCachePrefix);
        redisUtil.deletePattern(adminCachePrefix);

        // 7) 幂等写入，窗口内不再重复跑（SYSTEM 延迟消息 / 用户手动取消，都会命中）
        markIdempotent(orderId);

        log.info("[cancelOrder] 订单 {} 取消成功，归还 {} 个订单项库存，操作人={}", orderId, items.size(), operatorRole);
        return true;
    }

    private void markIdempotent(Long orderId) {
        redisUtil.set(ORDER_CANCEL_IDEMPOTENT_PREFIX + orderId, "1", idempotentTtlSeconds, TimeUnit.SECONDS);
    }
}
