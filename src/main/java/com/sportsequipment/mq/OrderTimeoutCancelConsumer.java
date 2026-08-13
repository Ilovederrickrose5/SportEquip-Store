package com.sportsequipment.mq;

import com.rabbitmq.client.Channel;
import com.sportsequipment.config.RabbitMQConfig;
import com.sportsequipment.dto.mq.OrderPendingTimeoutEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 未支付订单超时自动取消消费者。
 * 消息来自 order.delay.queue 的 DLX（TTL 到期进入 order.cancel.queue）。
 * 业务逻辑统一委托 OrderCancelService：该类同时对"用户手动取消订单"复用，保证手动和自动两条路径逻辑完全一致。
 */
@Component
public class OrderTimeoutCancelConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutCancelConsumer.class);

    private final OrderCancelService orderCancelService;

    public OrderTimeoutCancelConsumer(OrderCancelService orderCancelService) {
        this.orderCancelService = orderCancelService;
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_CANCEL_QUEUE)
    public void onPendingTimeout(OrderPendingTimeoutEvent event, Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        if (event == null || event.getOrderId() == null) {
            channel.basicAck(tag, false);
            return;
        }

        try {
            // SYSTEM 角色：跳权限，只允许取消 PENDING；幂等+库存归还+缓存清理 OrderCancelService 内部都做了
            orderCancelService.cancelOrderAndRestoreStock(event.getOrderId(), "SYSTEM", null, true);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("[order.cancel] 消费失败 orderId={}", event.getOrderId(), e);
            // 已重投一次则放弃，避免死循环
            if (message.getMessageProperties().getRedelivered()) {
                log.error("[order.cancel] 已重投 1 次仍失败，orderId={} 拒绝入队，等待人工排查", event.getOrderId());
                channel.basicNack(tag, false, false);
            } else {
                channel.basicNack(tag, false, true);
            }
        }
    }
}
