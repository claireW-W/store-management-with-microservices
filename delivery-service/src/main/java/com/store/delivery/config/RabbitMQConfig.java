package com.store.delivery.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RabbitMQConfig {
    
    // Exchanges
    public static final String DELIVERY_EXCHANGE = "delivery.exchange";
    public static final String ORDER_EXCHANGE = "order.exchange";
    
    // Queues
    public static final String DELIVERY_STATUS_UPDATE_QUEUE = "delivery.status.queue";
    public static final String DELIVERY_ORDER_PAID_QUEUE = "delivery.order.paid.queue";
    public static final String DELIVERY_ORDER_CANCEL_QUEUE = "delivery.order.cancel.queue";
    
    // Routing Keys
    public static final String DELIVERY_STATUS_ROUTING_KEY = "delivery.status.#";
    public static final String ORDER_PAID_ROUTING_KEY = "order.paid";
    public static final String ORDER_CANCELLED_ROUTING_KEY = "order.cancelled";
    
    @Value("${rabbitmq.queues.status-update:delivery.status.queue}")
    private String statusUpdateQueue;
    
    @Bean
    public TopicExchange deliveryExchange() {
        return new TopicExchange(DELIVERY_EXCHANGE);
    }
    
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }
    
    // Delivery Status Update Queue (for sending status updates to other services)
    @Bean
    public Queue deliveryStatusQueue() {
        return new Queue(DELIVERY_STATUS_UPDATE_QUEUE, true);
    }
    
    // Order Paid Queue (for receiving paid order notifications)
    @Bean
    public Queue deliveryOrderPaidQueue() {
        return new Queue(DELIVERY_ORDER_PAID_QUEUE, true);
    }
    
    @Bean
    public Queue deliveryOrderCancelQueue() {
        return new Queue(DELIVERY_ORDER_CANCEL_QUEUE, true);
    }
    
    @Bean
    public Binding orderPaidBinding(Queue deliveryOrderPaidQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(deliveryOrderPaidQueue).to(orderExchange).with(ORDER_PAID_ROUTING_KEY);
    }
    
    @Bean
    public Binding orderCancelledBinding(Queue deliveryOrderCancelQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(deliveryOrderCancelQueue).to(deliveryExchange).with(ORDER_CANCELLED_ROUTING_KEY);
    }
    
    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
    
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }
}
