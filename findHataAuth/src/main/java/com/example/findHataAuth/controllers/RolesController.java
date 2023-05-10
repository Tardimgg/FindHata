package com.example.findHataAuth.controllers;

//import com.example.findHataAuth.Result;
import com.example.Result;
import com.example.findHataAuth.entities.requests.ChangeRoleRequest;
import com.example.findHataAuth.entities.requests.GetRoleWithTokenRequest;
import com.example.findHataAuth.entities.requests.GetRolesRequest;
import com.example.findHataAuth.entities.Role;
import com.example.findHataAuth.entities.ShortInfoUser;
import com.example.findHataAuth.services.UserService;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class RolesController {

    @Autowired
    UserService userService;


    @PostMapping("/get-all")
    public Map<String, Object> getRoles(@Validated @NotEmpty @RequestBody GetRolesRequest request) {
        Result<List<Role>, String> roles = userService.getRoles(request.getAccessToken(), request.getUserId());

        if (roles.type == Result.Val.Ok) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            response.put("userId", request.getUserId());
            response.put("roles", roles.ok);
            return response;

        } else {
            return Map.of("status", "error", "error", roles.err);
        }
    }


    @PostMapping("/get-all-with-token")
    public Map<String, Object> getRoles(@Validated @NotEmpty @RequestBody GetRoleWithTokenRequest request) {
        Result<ShortInfoUser, String> roles = userService.getRoles(request.getAccessToken(), request.getUserToken());

        if (roles.type == Result.Val.Ok) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            response.put("userId", roles.ok.getId());
            response.put("roles", roles.ok.getRoles());
            response.put("hasAlternativeConnection", roles.ok.isHasAlternativeConnection());
            return response;

        } else {
            return Map.of("status", "error", "error", roles.err);
        }
    }

    @DeleteMapping
    public Map<String, String> removeRole(@Validated @NotEmpty @RequestBody ChangeRoleRequest request) {
        Result<Integer, String> res = userService.removeRole(request.getAccessToken(), request.getUserId(), request.getRole());

        if (res.type == Result.Val.Ok) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "ok");
            response.put("userId", res.ok.toString());
            return response;
        } else {
            return Map.of("status", "error", "error", res.err);
        }
    }

    @PostMapping
    public Map<String, String> addRole(@Validated @NotEmpty @RequestBody ChangeRoleRequest request) {
        Result<Integer, String> res = userService.addRole(request.getAccessToken(), request.getUserId(), request.getRole());

        if (res.type == Result.Val.Ok) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "ok");
            response.put("userId", res.ok.toString());
            return response;
        } else {
            return Map.of("status", "error", "error", res.err);
        }
    }
}
