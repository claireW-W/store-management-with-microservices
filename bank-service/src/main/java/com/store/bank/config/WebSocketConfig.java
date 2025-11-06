package com.store.bank.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration for real-time communication
 * Enables STOMP protocol over WebSocket for bidirectional messaging
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    /**
     * Configure message broker
     * /topic - for broadcasting to multiple subscribers
     * /queue - for point-to-point messaging
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for /topic and /queue destinations
        config.enableSimpleBroker("/topic", "/queue");
        
        // Set application destination prefix for @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        
        // Set user destination prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }
    
    /**
     * Register STOMP endpoints
     * Clients connect to this endpoint to establish WebSocket connection
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register /ws endpoint
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Allow all origins in development
                .withSockJS(); // Enable SockJS fallback for browsers without WebSocket support
        
        // Also register without SockJS for native WebSocket clients
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}

