package com.example.findHataNotificationServer.entities.requests;

import com.example.findHataNotificationServer.entities.Endpoint;
import com.example.findHataNotificationServer.entities.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AddEndpointRequest {


    @NotNull
    Integer userId;

    @NotNull
    List<EndpointRequest> endpoints;
}


