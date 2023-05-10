package com.example.findHataAuth.entities.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

//@JsonDeserialize
//public record PureUser (String login, String password) {}

//@JsonDeserialize
//@JsonSerialize
//@JsonComponent
@Data
@Builder
public class PureUser {

//    public String userId;
//    public String accessToken;
//    @JsonProperty(namespace = "login")
    @NotNull
    public String login;

//    @JsonProperty(namespace = "password")
    @NotNull
    public String password;
}