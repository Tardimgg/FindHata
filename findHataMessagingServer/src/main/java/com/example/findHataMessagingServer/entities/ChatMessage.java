package com.example.findHataMessagingServer.entities;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class ChatMessage {

    @NonNull
    Integer proposalId;

    @NonNull
    Integer toId;

    @NonNull
    String message;
}
