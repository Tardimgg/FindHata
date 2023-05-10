package com.example.findHataAuth.entities.requests;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class InternalRegistrationRequest {

    @NotNull
    String registrationToken;
}
