package com.example.findHataMessagingServer.entities.requests;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class GetAllWithTokenResponse {

    @NonNull
    String status;

    Integer userId;

    String error;
}
