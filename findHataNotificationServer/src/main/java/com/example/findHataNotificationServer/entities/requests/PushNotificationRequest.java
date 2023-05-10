package com.example.findHataNotificationServer.entities.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PushNotificationRequest {

    @NotNull
    Integer toUserId;

    @NotNull
    String title;

    @NotNull
    String message;

}
