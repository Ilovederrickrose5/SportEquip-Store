package com.sportsequipment.mq;

import com.rabbitmq.client.Channel;
import com.sportsequipment.config.RabbitMQConfig;
import com.sportsequipment.dto.mq.OrderCreatedEvent;
import com.sportsequipment.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 订单创建事件消费者：
 * 异步处理下单成功后那些"可以延迟做、失败也不影响主流程"的动作：
 * 1) 清理该用户的订单列表缓存 + 管理员订单列表缓存（保证列表页读到新订单）
 * 2) 清空该用户购物车 Redis 缓存（购物车 checkout 后应当清）
 * 3) 清除热门/随机商品缓存（销量已变，下一次读 DB 才会得到新的销量排序）
 *
 * 以上动作原本写在 createOrder 同步链路，MQ 化后主接口响应更快。
 */
@Component
public class OrderCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedConsumer.class);
    private static final String IDEMPOTENT_PREFIX = "mq:idempotent:order-created:";

    private final RedisUtil redisUtil;
    private final int idempotentTtlSeconds;

    public OrderCreatedConsumer(RedisUtil redisUtil, Environment env) {
        this.redisUtil = redisUtil;
        this.idempotentTtlSeconds = Integer.parseInt(env.getProperty("sportsequipment.mq.idempotent-ttl-seconds", "86400"));
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void onOrderCreated(OrderCreatedEvent event, Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        if (event == null || event.getOrderId() == null) {
            channel.basicAck(tag, false);
            return;
        }
        String idempotentKey = IDEMPOTENT_PREFIX + event.getOrderId();

        try {
            // Redis SET NX：原子幂等。返回 null=首次，返回 1=已存在
            Boolean first = redisUtil.setIfAbsent(idempotentKey, "1", idempotentTtlSeconds, TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(first)) {
                log.info("[order.created][幂等] orderId={} 已处理，跳过", event.getOrderId());
                channel.basicAck(tag, false);
                return;
            }

            Long userId = event.getUserId();
            // 1) 订单列表缓存：user 维度 + admin 维度
            redisUtil.deletePattern("order:list:user:" + userId + ":*");
            redisUtil.deletePattern("order:list:admin:*");

            // 2) 清空用户购物车缓存
            redisUtil.delete("cart:user:" + userId);

            // 3) 热门/随机商品缓存：销量排序依据订单，清空让下次重建
            redisUtil.deletePattern("product:hot:*");
            redisUtil.deletePattern("product:random:*");

            log.info("[order.created] orderId={}, userId={} 异步清理完成", event.getOrderId(), userId);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            // 幂等失败才进入这里；幂等 key 还没生效允许重投 1 次，超过就记日志放弃（否则死循环）
            log.error("[order.created] 消费失败 orderId={}", event.getOrderId(), e);
            if (message.getMessageProperties().getRedelivered()) {
                log.error("[order.created] 已重投过 1 次仍失败，orderId={}，拒绝入队", event.getOrderId());
                redisUtil.delete(idempotentKey);
                channel.basicNack(tag, false, false);
            } else {
                redisUtil.delete(idempotentKey);
                channel.basicNack(tag, false, true);
            }
        }
    }
}
