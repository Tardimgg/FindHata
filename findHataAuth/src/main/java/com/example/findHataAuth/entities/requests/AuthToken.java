package com.example.findHataAuth.entities.requests;

import jakarta.validation.constraints.NotNull;

public class AuthToken {

    @NotNull
    public String accessToken;
}
