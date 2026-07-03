package com.climatizacion.sistema_clima.config;

import com.climatizacion.sistema_clima.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/queue", "/topic");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
                .setAllowedOrigins(
                        "http://localhost:5500",
                        "http://127.0.0.1:5500",
                        "https://clinquant-tulumba-124b74.netlify.app"
                )
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        String query = request.getURI().getQuery();
                        if (query != null && query.contains("token=")) {
                            String token = query.substring(query.indexOf("token=") + 6);
                            if (token.contains("&")) {
                                token = token.substring(0, token.indexOf("&"));
                            }
                            try {
                                if (jwtUtil.validateToken(token)) {
                                    String email = jwtUtil.extractEmail(token);
                                    Long idUsuario = jwtUtil.extractIdUsuario(token);
                                    attributes.put("email", email);
                                    attributes.put("idUsuario", idUsuario);
                                    return true;
                                }
                            } catch (Exception e) {
                                System.err.println("Token inválido en WebSocket: " + e.getMessage());
                            }
                        }
                        return false;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                               WebSocketHandler wsHandler, Exception exception) {
                    }
                })
                // 👇 ESTA ES LA PIEZA MÁGICA QUE FALTABA 👇
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        Long idUsuario = (Long) attributes.get("idUsuario");
                        if (idUsuario != null) {
                            // Devolvemos el ID del usuario como nombre del Principal.
                            // Esto es VITAL para que Spring sepa a quién enrutar el mensaje.
                            return () -> String.valueOf(idUsuario);
                        }
                        return null;
                    }
                })
                .withSockJS();
    }
}