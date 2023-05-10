package com.example.findHataAuth.controllers;

//import com.example.findHataAuth.Result;
import com.example.Result;
import com.example.findHataAuth.entities.requests.CheckUserRequest;
import com.example.findHataAuth.entities.requests.GetAllUsersRequest;
import com.example.findHataAuth.services.UserService;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UsersController {


    @Autowired
    UserService userService;


    @PostMapping("/get-all")
    public Map<String, Object> getUsers(@Validated @NotEmpty @RequestBody GetAllUsersRequest request) {
        Result<List<String>, String> users = userService.getAllUsers(request.getAccessToken());

        if (users.type == Result.Val.Ok) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            response.put("users", users.ok);
            return response;

        } else {
            return Map.of("status", "error" , "error", users.err);
        }
    }

    @PostMapping("/check-id")
    public Map<String, Object> checkId(@Validated @NotEmpty @RequestBody CheckUserRequest request) {
        Result<String, String> users = userService.checkId(request.getAccessToken(), request.getId());

        if (users.type == Result.Val.Ok) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            return response;

        } else {
            return Map.of("status", "error" , "error", users.err);
        }
    }

    @GetMapping("/confirm-alternative-connection")
    public Map<String, Object> confirmAlternativeConnection(@RequestParam String token) {
        Result<String, String> res = userService.confirmAlternativeConnection(token);

        if (res.type == Result.Val.Ok) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            return response;

        } else {
            return Map.of("status", "error", "error", res.err);
        }
    }
}
