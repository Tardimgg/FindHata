package com.example.findHataNotificationServer.controllers;

//import com.example.findHataNotificationServer.Result;
import com.example.Result;
import com.example.findHataNotificationServer.entities.Endpoint;
import com.example.findHataNotificationServer.entities.Role;
import com.example.findHataNotificationServer.entities.requests.AddEndpointRequest;
import com.example.findHataNotificationServer.entities.requests.PushNotificationRequest;
import com.example.findHataNotificationServer.services.EndpointService;
import com.example.findHataNotificationServer.services.sender.SenderType;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/endpoint")
public class EndpointController {

    @Autowired
    EndpointService endpointService;

    @PostMapping
    Map<String, String> addEndpoint(@RequestHeader String roles,
                                    @RequestHeader Integer userId,
                                    @Validated @NotEmpty @RequestBody AddEndpointRequest request) {

        List<Role> listRoles = Arrays.stream(roles.substring(1, roles.length() - 1)
                        .split(", "))
                .map(Role::valueOf)
                .toList();


        List<Endpoint> endpoints;
        try {
            endpoints = request.getEndpoints().stream()
                    .map((v) -> {
                        try {
                            return Endpoint.builder()
                                    .url(v.getUrl())
                                    .typeCommunication(SenderType.valueOf(v.getTypeCommunication()))
                                    .build();

                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                            throw new IllegalArgumentException(v.getTypeCommunication());
                        }
                    })
                    .toList();

        } catch (RuntimeException e) {
            return Map.of("status", "error", "error", e.getMessage() + " is not available");
        }


        Result<String, String> res = endpointService.addAllEndpoints(listRoles, userId, request.getUserId(), endpoints);

        if (res.type == Result.Val.Ok) {
            return Map.of("status", "ok");
        }
        return Map.of("status", "error", "error", res.err);
    }
}
