package com.example.findHataMessagingServer.controllers;

import com.example.findHataMessagingServer.entities.ChatMessage;
import com.example.findHataMessagingServer.entities.requests.GetAllWithTokenResponse;
import com.example.findHataMessagingServer.services.MessageService;
import com.example.webApiClient.CustomInserter;
import com.example.webApiClient.WebApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    MessageService messageService;

    @Autowired
    WebApiClient webApiClient;


    @MessageMapping("/chat")
    public void broadcastNews(@Payload ChatMessage message, SimpMessageHeaderAccessor accessor) {
        WebClient webClient = webApiClient.getWebClient("http://gateway-server/auth/api/roles/").block();

        Map<String, String> getAllRequest = new HashMap<>();
        getAllRequest.put("userToken", accessor.getFirstNativeHeader("login"));
        getAllRequest.put("accessToken", webApiClient.getServiceUserToken());

        GetAllWithTokenResponse response = webClient.post()
                .uri("get-all-with-token")
                .body(CustomInserter.fromValue(getAllRequest))
                .retrieve()
                .bodyToMono(GetAllWithTokenResponse.class).block();

        if (response == null || response.getUserId().equals(message.getToId())) {
            return;
        }

        messageService.send(message.getProposalId(), response.getUserId(), message.getToId(), message.getMessage());


        messagingTemplate.convertAndSendToUser(
                message.getProposalId() + "_" + response.getUserId() + "_" + message.getToId(),
                "/queue/messages",
                message);
    }

}
