package com.example.findHataMessagingServer.configs;

import com.example.findHataMessagingServer.entities.requests.GetAllWithTokenResponse;
import com.example.webApiClient.CustomInserter;
import com.example.webApiClient.WebApiClient;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer implements WebSocketMessageBrokerConfigurer {


    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }

    CompositeMessageConverter converter;

    @Autowired
    void setConverter(@Lazy CompositeMessageConverter converter) {
        this.converter = converter;
    }

    @Override
    public void customizeClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {


                StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
                if (StompCommand.SUBSCRIBE.equals(headerAccessor.getCommand())) {

                    String login = headerAccessor.getLogin();

                    if (!validateSubscription(login, Objects.requireNonNull(headerAccessor.getDestination()))) {
                        throw new IllegalArgumentException("No permission for this topic");
                    }
                }

                return message;
            }
        });
    }


    @Autowired
    WebApiClient client;

    private boolean validateSubscription(String token, String topicDestination) {
        try {
            WebClient webClient = client.getWebClient("http://gateway-server/auth/api/roles/").block();

            Map<String, String> getAllRequest = new HashMap<>();
            getAllRequest.put("userToken", token);
            getAllRequest.put("accessToken", client.getServiceUserToken());

            GetAllWithTokenResponse response = webClient.post()
                    .uri("get-all-with-token")
                    .body(CustomInserter.fromValue(getAllRequest))
                    .retrieve()
                    .bodyToMono(GetAllWithTokenResponse.class).block();

            if (response == null || !response.getStatus().equals("ok")) {
                return false;
            }

            return topicDestination.contains("_" + response.getUserId() + "/");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
//                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/room");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/room");
    }
}
