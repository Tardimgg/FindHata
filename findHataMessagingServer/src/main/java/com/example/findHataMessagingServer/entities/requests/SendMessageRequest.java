package com.example.findHataMessagingServer.entities.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotNull
    Integer proposalId;

    @NotNull
    Integer targetId;

    @NotNull
    String message;
}
