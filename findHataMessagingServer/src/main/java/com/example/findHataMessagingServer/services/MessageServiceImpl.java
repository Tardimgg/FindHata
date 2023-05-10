package com.example.findHataMessagingServer.services;

import com.example.Result;
import com.example.findHataMessagingServer.entities.Chat;
import com.example.findHataMessagingServer.entities.Role;
import com.example.findHataMessagingServer.entities.Message;
import com.example.findHataMessagingServer.entities.responses.CheckIdResponse;
import com.example.findHataMessagingServer.entities.responses.RecentPostsResponse;
import com.example.findHataMessagingServer.repositories.ChatRepository;
import com.example.findHataMessagingServer.repositories.MessageRepository;
import com.example.webApiClient.CustomInserter;
import com.example.webApiClient.WebApiClient;
import jakarta.persistence.criteria.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import reactor.core.scheduler.Schedulers;

import java.util.*;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    ChatRepository chatRepository;

    @Autowired
    private WebApiClient webApiClient;

    @Override
    public Result<String, String> send(Integer proposalId, Integer fromId, Integer toId, String message) {

        webApiClient.getWebClient("http://gateway-server/auth/api/")
                .flatMap((v) -> v.post()
                        .uri("users/check-id")
                        .body(CustomInserter.fromValue(Map.of(
                                "id", toId,
                                "accessToken", webApiClient.getServiceUserToken()
                        ))).retrieve()
                        .bodyToMono(CheckIdResponse.class)
                        .map((response) -> {
                            if (response.getStatus().equals("ok")) {

                                String chatId = proposalId + "_" +
                                        Integer.min(fromId, toId) + "_" + Integer.max(fromId, toId);

                                Chat chat = chatRepository.findById(chatId)
                                        .orElseGet(() -> Chat.builder()
                                        .pfsId(chatId)
                                        .messages(new ArrayList<>())
                                        .build());

                                Message messageBD = Message.builder()
                                        .fromId(fromId)
                                        .toId(toId)
                                        .time(System.currentTimeMillis())
                                        .message(message)
                                        .read(false)
                                        .build();


                                messageRepository.save(messageBD);

                                chat.getMessages().add(messageBD);
                                chatRepository.save(chat);
                            }
                            return "";
                        }))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

        return Result.ok("in process");
    }

    @Override
    public Result<List<Message>, String> getCorrespondence(Integer proposalId, Integer questionerId, Integer withId) {
        return getCorrespondence(proposalId, questionerId, withId, -1L);
    }

    @Override
    public Result<List<Message>, String> getCorrespondence(Integer proposalId,
                                                           Integer questionerId, Integer withId, Long afterTime) {

        String chatId = proposalId + "_" +
                Integer.min(questionerId, withId) + "_" + Integer.max(questionerId, withId);

        Optional<Chat> oChat = chatRepository.findById(chatId);

        if (oChat.isEmpty()) {
            return Result.err("no messages found");
        }

        Chat chat = oChat.get();

        return Result.ok(chat.getMessages());
    }

    @Override
    public Result<List<RecentPostsResponse>, String> getRecentPosts(Integer questionerId) {
        List<Chat> messages = chatRepository.findAll((Specification<Chat>) (root, query, criteriaBuilder) -> {
            Predicate first = criteriaBuilder.like(root.get("pfsId").as(String.class), "%_" + questionerId + "_%");

            Predicate second = criteriaBuilder.like(root.get("pfsId").as(String.class),  "%_" + questionerId);

            return criteriaBuilder.or(first, second);
        });

        List<RecentPostsResponse> res = new ArrayList<>();

        for (Chat chat: messages) {
            Optional<Message> max = chat.getMessages().stream().max(Comparator.comparing(Message::getTime));
            max.ifPresent((v) -> res.add(RecentPostsResponse.builder()
                    .message(v)
                    .proposalId(Integer.parseInt(chat.getPfsId().substring(0, chat.getPfsId().indexOf("_"))))
                    .build()));
        }

        return Result.ok(res);
    }

    @Override
    public Result<List<Message>, String> getCorrespondence(List<Role> roles, Integer proposalId,
                                                           Integer firstId, Integer secondId) {
        if (roles.contains(Role.ADMIN)) {
            return getCorrespondence(proposalId, firstId, secondId);
        } else {
            return Result.err("The user does not have the necessary roles");
        }
    }
}
