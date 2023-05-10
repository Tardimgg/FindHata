package com.example.findHataMessagingServer.controllers;

import com.example.Result;
import com.example.findHataMessagingServer.entities.Message;
import com.example.findHataMessagingServer.entities.Role;
import com.example.findHataMessagingServer.entities.requests.SendMessageRequest;
import com.example.findHataMessagingServer.entities.responses.RecentPostsResponse;
import com.example.findHataMessagingServer.services.MessageService;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    MessageService messageService;

    @PostMapping("/send")
    Map<String, String> send(@RequestHeader String roles,
                             @RequestHeader Integer userId,
                             @RequestHeader String hasAlternativeConnection,
                             @Validated @NotEmpty @RequestBody SendMessageRequest request) {


        if (userId.equals(request.getTargetId())) {
            return Map.of("status", "error", "error", "you can't send messages to yourself");
        }

        List<Role> listRoles = Arrays.stream(roles.substring(1, roles.length() - 1)
                        .split(", "))
                .map(Role::valueOf)
                .toList();

        if (!(listRoles.contains(Role.ADMIN) || listRoles.contains(Role.OTHER_SERVICE)
                || listRoles.contains(Role.USER))) {
            return Map.of("status", "error", "error", "the user is not registered");
        }

        Result<String, String> res = messageService.send(
                request.getProposalId(),
                userId,
                request.getTargetId(),
                request.getMessage()
        );

        if (res.type == Result.Val.Ok) {
            return Map.of("status", "ok", "message", res.ok);
        }
        return Map.of("status", "error", "error", res.err);
    }

    @GetMapping("/get-all")
    Map<String, Object> getAll(@RequestHeader String roles,
                               @RequestHeader Integer userId,
                               @RequestParam Integer withId,
                               @RequestParam Integer proposalId,
                               @RequestParam(required = false) Long afterTime) {

        List<Role> listRoles = Arrays.stream(roles.substring(1, roles.length() - 1)
                        .split(", "))
                .map(Role::valueOf)
                .toList();

        if (listRoles.contains(Role.ANONYMOUS) && listRoles.size() == 1 || listRoles.size() == 0) {
            return Map.of("status", "error", "error", "access denied");
        }


        Result<List<Message>, String> messages;
        if (afterTime == null) {
            messages = messageService.getCorrespondence(proposalId, userId, withId);
        } else {
            messages = messageService.getCorrespondence(proposalId, userId, withId, afterTime);
        }

        if (messages.type == Result.Val.Ok) {
            return Map.of("status", "ok", "messages", messages.ok);
        }
        return Map.of("status", "error", "error", messages.err);

    }

    @GetMapping("/get-all-small")
    Map<String, Object> getAllSmall(@RequestHeader String roles,
                                    @RequestHeader Integer userId) {


        List<Role> listRoles = Arrays.stream(roles.substring(1, roles.length() - 1)
                        .split(", "))
                .map(Role::valueOf)
                .toList();

        if (listRoles.contains(Role.ANONYMOUS) && listRoles.size() == 1 || listRoles.size() == 0) {
            return Map.of("status", "error", "error", "access denied");
        }

        Result<List<RecentPostsResponse>, String> allMessages = messageService.getRecentPosts(userId);

        if (allMessages.type == Result.Val.Ok) {
            return Map.of("status", "ok", "recentPosts", allMessages.ok);
        }
        return Map.of("status", "error", "error", allMessages.err);

    }

    @GetMapping("/get-correspondence")
    Map<String, Object> getCorrespondence(@RequestHeader String roles,
                                          @RequestParam Integer firstId,
                                          @RequestParam Integer secondId,
                                          @RequestParam Integer proposalId) {

        List<Role> listRoles = Arrays.stream(roles.substring(1, roles.length() - 1)
                        .split(", "))
                .map(Role::valueOf)
                .toList();

        Result<List<Message>, String> messages = messageService.getCorrespondence(listRoles, proposalId,
                firstId, secondId);

        if (messages.type == Result.Val.Ok) {
            return Map.of("status", "ok", "messages", messages.ok);
        }
        return Map.of("status", "error", "error", messages.err);
    }
}
