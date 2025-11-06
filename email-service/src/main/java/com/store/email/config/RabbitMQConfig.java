package com.store.email.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchanges
    public static final String EMAIL_EXCHANGE = "email.exchange";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String BANK_EXCHANGE = "bank.exchange";
    public static final String DELIVERY_EXCHANGE = "delivery.exchange";

    // Queues
    public static final String EMAIL_ORDER_CONFIRMATION_QUEUE = "email.order.confirmation.queue";
    public static final String EMAIL_PAYMENT_NOTIFICATION_QUEUE = "email.payment.notification.queue";
    public static final String EMAIL_DELIVERY_UPDATE_QUEUE = "email.delivery.update.queue";
    public static final String EMAIL_REFUND_NOTIFICATION_QUEUE = "email.refund.notification.queue";

    // Routing Keys
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String PAYMENT_SUCCESS_ROUTING_KEY = "bank.payment.success";
    public static final String PAYMENT_FAILURE_ROUTING_KEY = "bank.payment.failure";
    public static final String DELIVERY_STATUS_ROUTING_KEY = "delivery.status.#";
    public static final String REFUND_SUCCESS_ROUTING_KEY = "bank.refund.success";

    @Bean
    public TopicExchange emailExchange() {
        return new TopicExchange(EMAIL_EXCHANGE);
    }

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public TopicExchange bankExchange() {
        return new TopicExchange(BANK_EXCHANGE);
    }

    @Bean
    public TopicExchange deliveryExchange() {
        return new TopicExchange(DELIVERY_EXCHANGE);
    }

    // Order Confirmation Queue
    @Bean
    public Queue emailOrderConfirmationQueue() {
        return new Queue(EMAIL_ORDER_CONFIRMATION_QUEUE, true);
    }

    @Bean
    public Binding orderConfirmationBinding(Queue emailOrderConfirmationQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(emailOrderConfirmationQueue).to(orderExchange).with(ORDER_CREATED_ROUTING_KEY);
    }

    // Payment Notification Queue
    @Bean
    public Queue emailPaymentNotificationQueue() {
        return new Queue(EMAIL_PAYMENT_NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Binding paymentSuccessBinding(Queue emailPaymentNotificationQueue, TopicExchange bankExchange) {
        return BindingBuilder.bind(emailPaymentNotificationQueue).to(bankExchange).with(PAYMENT_SUCCESS_ROUTING_KEY);
    }

    @Bean
    public Binding paymentFailureBinding(Queue emailPaymentNotificationQueue, TopicExchange bankExchange) {
        return BindingBuilder.bind(emailPaymentNotificationQueue).to(bankExchange).with(PAYMENT_FAILURE_ROUTING_KEY);
    }

    // Delivery Update Queue
    @Bean
    public Queue emailDeliveryUpdateQueue() {
        return new Queue(EMAIL_DELIVERY_UPDATE_QUEUE, true);
    }

    @Bean
    public Binding deliveryUpdateBinding(Queue emailDeliveryUpdateQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(emailDeliveryUpdateQueue).to(deliveryExchange).with(DELIVERY_STATUS_ROUTING_KEY);
    }

    // Refund Notification Queue
    @Bean
    public Queue emailRefundNotificationQueue() {
        return new Queue(EMAIL_REFUND_NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Binding refundNotificationBinding(Queue emailRefundNotificationQueue, TopicExchange bankExchange) {
        return BindingBuilder.bind(emailRefundNotificationQueue).to(bankExchange).with(REFUND_SUCCESS_ROUTING_KEY);
    }

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

