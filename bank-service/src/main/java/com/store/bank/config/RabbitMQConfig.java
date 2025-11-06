package com.store.bank.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Bank Service
 * Defines exchanges, queues, and bindings for asynchronous messaging
 */
@Configuration
public class RabbitMQConfig {
    
    // ==========================================
    // Exchange Names
    // ==========================================
    public static final String BANK_EXCHANGE = "bank.exchange";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String DELIVERY_EXCHANGE = "delivery.exchange";
    
    // ==========================================
    // Queue Names
    // ==========================================
    public static final String REFUND_QUEUE = "bank.refund.queue";
    public static final String PAYMENT_NOTIFICATION_QUEUE = "bank.payment.notification.queue";
    
    // ==========================================
    // Routing Keys
    // ==========================================
    public static final String REFUND_ROUTING_KEY = "bank.refund.*";
    public static final String LOST_PACKAGE_ROUTING_KEY = "delivery.lost";
    public static final String ORDER_CANCELLED_ROUTING_KEY = "order.cancelled";
    
    /**
     * JSON Message Converter
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    /**
     * RabbitTemplate with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
    
    // ==========================================
    // Bank Exchange and Queues
    // ==========================================
    
    /**
     * Bank Topic Exchange
     */
    @Bean
    public TopicExchange bankExchange() {
        return new TopicExchange(BANK_EXCHANGE);
    }
    
    /**
     * Refund Queue - receives refund requests
     */
    @Bean
    public Queue refundQueue() {
        return QueueBuilder.durable(REFUND_QUEUE)
                .withArgument("x-message-ttl", 3600000) // 1 hour TTL
                .withArgument("x-max-length", 10000)     // Max 10k messages
                .build();
    }
    
    /**
     * Payment Notification Queue - sends payment confirmations
     */
    @Bean
    public Queue paymentNotificationQueue() {
        return QueueBuilder.durable(PAYMENT_NOTIFICATION_QUEUE)
                .withArgument("x-message-ttl", 3600000)
                .build();
    }
    
    /**
     * Bind Refund Queue to Bank Exchange
     */
    @Bean
    public Binding refundBinding() {
        return BindingBuilder
                .bind(refundQueue())
                .to(bankExchange())
                .with(REFUND_ROUTING_KEY);
    }
    
    // ==========================================
    // External Exchanges (for receiving messages)
    // ==========================================
    
    /**
     * Delivery Exchange (to receive lost package notifications)
     */
    @Bean
    public TopicExchange deliveryExchange() {
        return new TopicExchange(DELIVERY_EXCHANGE);
    }
    
    /**
     * Order Exchange (to receive order cancellation notifications)
     */
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }
    
    /**
     * Bind Refund Queue to Delivery Exchange (for lost packages)
     */
    @Bean
    public Binding lostPackageBinding() {
        return BindingBuilder
                .bind(refundQueue())
                .to(deliveryExchange())
                .with(LOST_PACKAGE_ROUTING_KEY);
    }
    
    /**
     * Bind Refund Queue to Order Exchange (for cancelled orders)
     */
    @Bean
    public Binding orderCancelledBinding() {
        return BindingBuilder
                .bind(refundQueue())
                .to(orderExchange())
                .with(ORDER_CANCELLED_ROUTING_KEY);
    }
}

