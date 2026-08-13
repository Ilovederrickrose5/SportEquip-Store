package com.sportsequipment.mq;

import com.sportsequipment.config.RabbitMQConfig;
import com.sportsequipment.dto.mq.OrderCreatedEvent;
import com.sportsequipment.dto.mq.OrderPendingTimeoutEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 统一生产者：封装业务事件发送，业务方只需传入 Event。
 */
@Component
public class MqEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(MqEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public MqEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发布「订单创建事件」到 order.created.queue，用于异步清理缓存/通知等解耦动作。
     */
    public void publishOrderCreated(OrderCreatedEvent event) {
        if (event == null || event.getOrderId() == null) return;
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, RabbitMQConfig.RK_ORDER_CREATED, event);
            log.info("[MQ→order.created] 发布事件 orderId={}, userId={}", event.getOrderId(), event.getUserId());
        } catch (Exception e) {
            // 发送失败不影响主流程：日志留痕，后续可补偿或升级为重试队列
            log.error("[MQ→order.created] 发布失败 orderId={}", event.getOrderId(), e);
        }
    }

    /**
     * 发布「待支付订单超时检查」到 order.delay.queue。
     * 该队列设置了 x-message-ttl，到期后消息进入 DLX，最终由 order.cancel.queue 的消费者处理。
     */
    public void publishOrderPendingTimeout(OrderPendingTimeoutEvent event) {
        if (event == null || event.getOrderId() == null) return;
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, RabbitMQConfig.RK_ORDER_DELAY, event);
            log.info("[MQ→order.delay] 发布延迟事件 orderId={}, ttlMs={}", event.getOrderId(), event.getTtlMs());
        } catch (Exception e) {
            log.error("[MQ→order.delay] 发布失败 orderId={}", event.getOrderId(), e);
        }
    }
}
