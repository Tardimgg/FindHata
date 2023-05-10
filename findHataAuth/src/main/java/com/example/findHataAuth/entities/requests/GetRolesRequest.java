package com.example.findHataAuth.entities.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GetRolesRequest {

    @NotNull
    Integer userId;

    @NotNull
    String accessToken;

}
