package com.example.findHataAuth.entities.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class RegistrationRequest {

    @NotNull
    public String login;

    @NotNull
    public String password;

    @NonNull
    String url;

    @NonNull
    String typeCommunication;
}
