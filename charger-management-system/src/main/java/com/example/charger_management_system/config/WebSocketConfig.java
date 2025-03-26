package com.example.charger_management_system.config;

import com.example.charger_management_system.websocket.OcppWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final OcppWebSocketHandler ocppWebSocketHandler;

    public WebSocketConfig(OcppWebSocketHandler ocppWebSocketHandler) {
        this.ocppWebSocketHandler = ocppWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(ocppWebSocketHandler, "/ocpp").setAllowedOrigins("*");
    }
}