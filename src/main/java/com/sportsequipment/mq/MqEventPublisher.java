package com.sportsequipment.mq;

import com.sportsequipment.config.RabbitMQConfig;
import com.sportsequipment.dto.mq.OrderCreatedEvent;
import com.sportsequipment.dto.mq.OrderPendingTimeoutEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * RabbitMQ 统一生产者：封装业务事件发送，业务方只需传入 Event。
 *
 * <p>【可观测性增强】：
 * 1) 每次 convertAndSend 都带 {@link CorrelationData}（id = "evt:{type}:{orderId}:{uuid}"），
 *    以便 RabbitTemplate 的 ConfirmCallback / ReturnsCallback 能将 broker 回调关联到具体业务 orderId。
 * 2) publish 方法返回 boolean 标志（true=同步发送未抛异常；false=发送前校验不通过或抛异常），
 *    让 Service 层不再对"MQ 是否真正发出去了"完全无感知。
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
     *
     * @return true=发送阶段未抛异常；false=event 为空 / orderId 为空 / 发送抛异常（异常会 error 留痕）
     */
    public boolean publishOrderCreated(OrderCreatedEvent event) {
        if (event == null || event.getOrderId() == null) {
            log.warn("[MQ→order.created] 入参为空或 orderId=null，跳过发布");
            return false;
        }
        String correlationId = "evt:orderCreated:" + event.getOrderId() + ":" + UUID.randomUUID();
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.RK_ORDER_CREATED,
                    event,
                    new CorrelationData(correlationId));
            log.info("[MQ→order.created] 发布事件成功 orderId={}, userId={}, correlation={}",
                    event.getOrderId(), event.getUserId(), correlationId);
            return true;
        } catch (Exception e) {
            // 发送失败不影响主流程：日志留痕，后续可补偿或升级为重试队列
            log.error("[MQ→order.created] 发布失败 orderId={}, correlation={}",
                    event.getOrderId(), correlationId, e);
            return false;
        }
    }

    /**
     * 发布「待支付订单超时检查」到 order.delay.queue。
     * 该队列设置了 x-message-ttl，到期后消息进入 DLX，最终由 order.cancel.queue 的消费者处理。
     *
     * @return true=发送阶段未抛异常；false=event 为空 / orderId 为空 / 发送抛异常
     */
    public boolean publishOrderPendingTimeout(OrderPendingTimeoutEvent event) {
        if (event == null || event.getOrderId() == null) {
            log.warn("[MQ→order.delay] 入参为空或 orderId=null，跳过发布延迟消息");
            return false;
        }
        String correlationId = "evt:orderDelay:" + event.getOrderId() + ":" + UUID.randomUUID();
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.RK_ORDER_DELAY,
                    event,
                    new CorrelationData(correlationId));
            log.info("[MQ→order.delay] 发布延迟事件成功 orderId={}, ttlMs={}, correlation={}",
                    event.getOrderId(), event.getTtlMs(), correlationId);
            return true;
        } catch (Exception e) {
            log.error("[MQ→order.delay] 发布失败 orderId={}, correlation={}",
                    event.getOrderId(), correlationId, e);
            return false;
        }
    }
}
