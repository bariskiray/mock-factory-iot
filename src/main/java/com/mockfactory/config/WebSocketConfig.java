package com.mockfactory.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket / STOMP configuration for the Mock Factory telemetry bus.
 * <p>
 * In a real plant, telemetry would flow over OPC-UA or MQTT.  Here we
 * use STOMP-over-WebSocket so any browser can subscribe to live device
 * data without extra dependencies.
 *
 * <ul>
 *   <li><b>Endpoint:</b> {@code /ws} (with SockJS fallback for older browsers)</li>
 *   <li><b>Broker prefix:</b> {@code /topic} — clients subscribe to
 *       {@code /topic/telemetry/{deviceId}}</li>
 *   <li><b>App prefix:</b> {@code /app} — reserved for future server-bound messages</li>
 * </ul>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Simple in-memory broker for the /topic destination prefix.
        registry.enableSimpleBroker("/topic");

        // Prefix for messages routed to @MessageMapping methods.
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // STOMP handshake endpoint — SockJS provides fallback transports.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
