package com.example.findHataAuth.controllers;

import com.auth0.jwt.interfaces.Claim;
//import com.example.findHataAuth.Result;
import com.example.Result;
import com.example.findHataAuth.entities.MyUser;
import com.example.findHataAuth.entities.requests.*;
import com.example.findHataAuth.entities.Role;
import com.example.findHataAuth.services.UserService;
import com.example.tokenService.TokenService;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RegistrationController {

    @Autowired
    UserService userService;

    @PostMapping("/registration")
    public Map<String, Object> registration(@Validated @NotEmpty @RequestBody RegistrationRequest user) {
        PureUser pureUser = PureUser.builder()
                .login(user.login)
                .password(user.password)
                .build();

        Result<Pair<String, MyUser>, String> jwtToken = userService.createUser(pureUser, List.of(Role.USER), user.getUrl(), user.getTypeCommunication());

        if (jwtToken.type == Result.Val.Ok) {
            return Map.of("status", "ok",
                    "accessToken", jwtToken.ok.getFirst(),
                    "roles", jwtToken.ok.getSecond().getRoles(),
                    "userId", jwtToken.ok.getSecond().getId()
            );
        } else {
            return Map.of("status", "error", "error", jwtToken.err);
        }
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Validated @NotEmpty @RequestBody PureUser user) {
        Result<Pair<String, MyUser>, String> jwtToken = userService.login(user);

        if (jwtToken.type == Result.Val.Ok) {
            return Map.of("status", "ok",
                    "accessToken", jwtToken.ok.getFirst(),
                    "roles", jwtToken.ok.getSecond().getRoles(),
                    "userId", jwtToken.ok.getSecond().getId()
            );
        } else {
            return Map.of("status", "error", "error", jwtToken.err);
        }
    }

    @PostMapping("/change-password")
    public Map<String, String> changePassword(@Validated @NotEmpty @RequestBody ChangePasswordRequest request) {
        Result<String, String> res = userService.changePassword(request.getAccessToken(), request.getNewPassword());

        if (res.type == Result.Val.Ok) {
            return Map.of("status", "ok");
        } else {
            return Map.of("error", res.err);
        }
    }

    @PostMapping("/check-token")
    public Map<String, Object> checkToken(@Validated @NotEmpty @RequestBody AuthToken token) {
        Result<Pair<Integer, Boolean>, String> jwtToken = userService.checkTokenAndUserId(token.accessToken);

        if (jwtToken.type == Result.Val.Ok) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            return response;
        } else {
            return Map.of("error", jwtToken.err);
        }
    }

    @Value("${internal_registration_jwt_secret}")
    private String internalJwtSecret;

    @PostMapping("/internal-registration")
    public Map<String, String> internalRegistration(@Validated @NotEmpty @RequestBody InternalRegistrationRequest request) {

        TokenService tokenService = new TokenService(internalJwtSecret);
        Map<String, Claim> json = tokenService.validateRawToken(request.getRegistrationToken());

        PureUser user = PureUser.builder()
                .login(json.get("serviceId").asString())
                .password(json.get("password").asString())
                .build();

        Result<String, String> jwtToken = userService.createInternalUser(user, List.of(Role.OTHER_SERVICE));

        if (jwtToken.type == Result.Val.Ok) {
            return Map.of("accessToken", jwtToken.ok);
        } else {
            return Map.of("error", jwtToken.err);
        }
    }


}
