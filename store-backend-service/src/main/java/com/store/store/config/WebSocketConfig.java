package com.store.store.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration for Store Backend Service
 * Enables real-time order status updates to frontend clients
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    /**
     * Configure message broker
     * /topic - for broadcasting to multiple subscribers (e.g., all users)
     * /queue - for point-to-point messaging (e.g., specific user)
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
        // Register /ws endpoint with SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000")
                .withSockJS();

        // Also register without SockJS for native WebSocket clients
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000");
    }
}

