package com.example.findHataAuth.entities.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckUserRequest {

    @NotNull
    String accessToken;

    @NotNull
    Integer id;

}
