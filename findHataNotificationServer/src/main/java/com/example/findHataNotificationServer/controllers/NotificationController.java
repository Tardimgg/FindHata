package com.example.findHataNotificationServer.controllers;

import com.example.Result;
import com.example.findHataNotificationServer.entities.Role;
import com.example.findHataNotificationServer.entities.requests.PushNotificationRequest;
import com.example.findHataNotificationServer.services.NotificationService;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    NotificationService userService;

    @PostMapping("/push")
    public Map<String, Object> push(@RequestHeader String roles,
                                    @RequestHeader Integer userId,
                                    @Validated @NotEmpty @RequestBody PushNotificationRequest request) {
        List<Role> listRoles = Arrays.stream(roles.substring(1, roles.length() - 1)
                .split(", "))
                .map(Role::valueOf)
                .toList();

        Result<String, String> res = userService.sendMessage(listRoles, request.getToUserId(), request.getTitle(), request.getMessage());

        if (res.type == Result.Val.Ok) {
            return Map.of("status", "ok");
        }
        return Map.of("status", "error", "error", res.err);
    }

}
