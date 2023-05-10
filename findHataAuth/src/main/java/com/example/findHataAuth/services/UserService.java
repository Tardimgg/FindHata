package com.example.findHataAuth.services;

//import com.example.findHataAuth.Result;
import com.example.Result;
import com.example.findHataAuth.entities.MyUser;
import com.example.findHataAuth.entities.requests.PureUser;
import com.example.findHataAuth.entities.Role;
import com.example.findHataAuth.entities.ShortInfoUser;
import org.springframework.data.util.Pair;

import java.util.List;

public interface UserService {

    Result<Pair<String, MyUser>, String> createUser(PureUser user, List<Role> roles, String url, String type);

    Result<String, String> createInternalUser(PureUser user, List<Role> roles);

    Result<Pair<String, MyUser>, String> login(PureUser user);

    Result<Pair<Integer, Boolean>, String> checkTokenAndUserId(String token);

    Result<String, String> confirmAlternativeConnection(String token);

    Result<List<Role>, String> getRoles(String accessToken, Integer userId);
    Result<ShortInfoUser, String> getRoles(String accessToken, String userToken);


    Result<Integer, String> removeRole(String accessToken, Integer userId, Role role);
    Result<Integer, String> addRole(String accessToken, Integer userId, Role role);

    Result<List<String>, String> getAllUsers(String accessToken);

    Result<String, String> changePassword(String accessToken, String newPassword);

    Result<String, String> checkId(String accessToken, Integer id);
}
