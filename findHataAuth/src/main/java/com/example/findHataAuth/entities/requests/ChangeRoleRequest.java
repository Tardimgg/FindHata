package com.example.findHataAuth.entities.requests;

import com.example.findHataAuth.entities.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeRoleRequest {

    @NotNull
    Integer userId;

    @NotNull
    String accessToken;

    @NotNull
    Role role;
}
