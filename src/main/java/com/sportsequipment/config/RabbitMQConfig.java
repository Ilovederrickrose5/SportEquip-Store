package com.sportsequipment.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 全局配置：
 *
 * 拓扑图：
 * order.exchange (direct)
 * ├─ order.created.queue (routingKey=order.created) → 异步做缓存清理、通知等
 * └─ order.delay.queue (routingKey=order.delay) → 带 x-message-ttl，过期后投递到 DLX
 *
 * order.dlx.exchange (direct) —— 死信交换机
 * └─ order.cancel.queue (routingKey=order.cancel) → 消费：PENDING 未支付自动取消 + 归还库存
 */
@Configuration
public class RabbitMQConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQConfig.class);

    // ========== 交换机 ==========
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_DLX_EXCHANGE = "order.dlx.exchange";

    // ========== 队列 ==========
    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";
    public static final String ORDER_CANCEL_QUEUE = "order.cancel.queue";

    // ========== 路由键 ==========
    public static final String RK_ORDER_CREATED = "order.created";
    public static final String RK_ORDER_DELAY = "order.delay";
    public static final String RK_ORDER_CANCEL = "order.cancel";

    @Value("${sportsequipment.mq.order-pending-ttl-ms:1800000}")
    private long orderPendingTtlMs;

    // ========== 交换机声明 ==========
    @Bean
    public DirectExchange orderExchange() {
        return ExchangeBuilder.directExchange(ORDER_EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange orderDlxExchange() {
        return ExchangeBuilder.directExchange(ORDER_DLX_EXCHANGE).durable(true).build();
    }

    // ========== 队列声明 ==========
    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE).build();
    }

    /**
     * 延迟队列：消息在本队列 TTL 到期后，会通过 x-dead-letter-exchange 投递到 order.dlx.exchange
     * 再通过 order.cancel 路由键进 order.cancel.queue。
     */
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", ORDER_DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", RK_ORDER_CANCEL);
        args.put("x-message-ttl", (int) orderPendingTtlMs);
        return QueueBuilder.durable(ORDER_DELAY_QUEUE).withArguments(args).build();
    }

    @Bean
    public Queue orderCancelQueue() {
        return QueueBuilder.durable(ORDER_CANCEL_QUEUE).build();
    }

    // ========== Binding ==========
    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(orderExchange).with(RK_ORDER_CREATED);
    }

    @Bean
    public Binding orderDelayBinding(Queue orderDelayQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderDelayQueue).to(orderExchange).with(RK_ORDER_DELAY);
    }

    @Bean
    public Binding orderCancelBinding(Queue orderCancelQueue, DirectExchange orderDlxExchange) {
        return BindingBuilder.bind(orderCancelQueue).to(orderDlxExchange).with(RK_ORDER_CANCEL);
    }

    // ========== 序列化：Jackson JSON，避免 Java 原生序列化 + 反序列化白名单问题 ==========
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @SuppressWarnings("null")
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate tpl = new RabbitTemplate(connectionFactory);
        tpl.setMessageConverter(messageConverter);
        // 生产者 confirm：消息到达 broker 交换机回调
        tpl.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.warn("[RabbitMQ] 消息未到达交换机，correlation={}, cause={}", correlationData, cause);
            }
        });
        // 生产者 return：消息从交换机能出来但没路由到队列
        tpl.setReturnsCallback(returned -> log.warn("[RabbitMQ] 消息未路由到队列，exchange={}, rk={}, reply={}, body={}",
                returned.getExchange(), returned.getRoutingKey(), returned.getReplyText(),
                new String(returned.getMessage().getBody())));
        return tpl;
    }

    /**
     * 消费者工厂：使用同一个 Jackson 序列化器，保证生产者/消费者的消息格式一致。
     */
    @Bean
    @SuppressWarnings("null")
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        // application.properties 中已声明 simple.*，这里只保证 converter 注入；显式设置确认模式为手动兜底
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }
}
