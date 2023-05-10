package com.example.findHataAuth.entities.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GetRoleWithTokenRequest {

    @NotNull
    String userToken;

    @NotNull
    String accessToken;

}
