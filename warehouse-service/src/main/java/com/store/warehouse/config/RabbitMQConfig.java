package com.store.warehouse.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Warehouse Service
 * Handles inventory-related messaging
 */
@Configuration
public class RabbitMQConfig {
    
    // ========== Exchanges ==========
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String WAREHOUSE_EXCHANGE = "warehouse.exchange";
    public static final String DELIVERY_EXCHANGE = "delivery.exchange";
    
    // ========== Queues ==========
    // Warehouse queues for receiving messages
    public static final String ORDER_EVENTS_QUEUE = "warehouse.order.events.queue";
    
    // ========== Routing Keys ==========
    // Order events (receive)
    public static final String ORDER_CREATED_KEY = "order.created";
    public static final String ORDER_PAID_KEY = "order.paid";
    public static final String ORDER_CANCELLED_KEY = "order.cancelled";
    
    // Warehouse events (send)
    public static final String WAREHOUSE_STOCK_RESERVED_KEY = "warehouse.stock.reserved";
    public static final String WAREHOUSE_STOCK_INSUFFICIENT_KEY = "warehouse.stock.insufficient";
    public static final String WAREHOUSE_STOCK_DEDUCTED_KEY = "warehouse.stock.deducted";
    public static final String WAREHOUSE_STOCK_UPDATED_KEY = "warehouse.stock.updated";
    
    // ========== Exchange Definitions ==========
    
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }
    
    @Bean
    public TopicExchange warehouseExchange() {
        return new TopicExchange(WAREHOUSE_EXCHANGE);
    }
    
    @Bean
    public TopicExchange deliveryExchange() {
        return new TopicExchange(DELIVERY_EXCHANGE);
    }
    
    // ========== Queue Definitions ==========
    
    @Bean
    public Queue orderEventsQueue() {
        return new Queue(ORDER_EVENTS_QUEUE, true); // Durable queue
    }
    
    // ========== Bindings ==========
    
    // Bind to Order Exchange - receive order events
    @Bean
    public Binding orderCreatedBinding(Queue orderEventsQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderEventsQueue)
                .to(orderExchange)
                .with(ORDER_CREATED_KEY);
    }
    
    @Bean
    public Binding orderPaidBinding(Queue orderEventsQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderEventsQueue)
                .to(orderExchange)
                .with(ORDER_PAID_KEY);
    }
    
    @Bean
    public Binding orderCancelledBinding(Queue orderEventsQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderEventsQueue)
                .to(orderExchange)
                .with(ORDER_CANCELLED_KEY);
    }
    
    // ========== Message Converter ==========
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}

