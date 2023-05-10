package com.example.findHataAuth.services;


import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
//import com.example.findHataAuth.Result;
import com.example.Result;
import com.example.findHataAuth.entities.MyUser;
import com.example.findHataAuth.entities.requests.PureUser;
import com.example.findHataAuth.entities.responses.AddEndpointResponse;
import com.example.findHataAuth.entities.Role;
import com.example.findHataAuth.entities.ShortInfoUser;
import com.example.findHataAuth.repositories.UserRepository;
//import com.example.findHataAuth.services.web.CustomInserter;
//import com.example.findHataAuth.services.web.WebApiClient;
import com.example.tokenService.TokenService;
//import com.example.tokenService.TokenService;
import com.example.webApiClient.CustomInserter;
import com.example.webApiClient.WebClientFilter;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Value("${login_jwt_secret}")
    String loginJwtSecret;

    @Value("${externalUrl}")
    String externalUrl;

    @Value("${service_login}")
    String serviceLogin;

    @Value("${service_password}")
    String servicePassword;

    @Value("${admin_login}")
    String adminLogin;

    @Value("${admin_password}")
    String adminPassword;

    @PostConstruct
    private void init() {
        PureUser admin = PureUser.builder()
                .login(adminLogin)
                .password(adminPassword)
                .build();

        createInternalUser(admin, List.of(Role.ADMIN));

        PureUser service = PureUser.builder()
                .login(serviceLogin)
                .password(servicePassword)
                .build();

        createInternalUser(service, List.of(Role.OTHER_SERVICE));
    }

    @Autowired
    private WebClientFilter webClientFilter;

    public Result<Pair<String, MyUser>, String> createUser(PureUser user, List<Role> roles, String url, String type) {
        String hashedPassword = encoder.encode(user.password);

        MyUser foundUser = userRepository.findUserByLogin(user.login);

        if (foundUser == null) {
            MyUser createdUser = userRepository.save(MyUser.builder()
                    .login(user.login)
                    .hashedPassword(hashedPassword)
                    .roles(roles)
                    .build()
            );

            Map<String, Object> addRequest = new HashMap<>();
            addRequest.put("userId", createdUser.getId());

            List<Map<String, ?>> endpoints = new ArrayList<>();
            endpoints.add(Map.of("url", url, "typeCommunication", type));

            addRequest.put("endpoints", endpoints);

            PureUser service = PureUser.builder()
                    .login(serviceLogin)
                    .password(servicePassword)
                    .build();

            WebClient webClient = WebClient.builder()
                    .filter(webClientFilter)
                    .defaultHeader("accessToken", login(service).ok.getFirst())
                    .baseUrl("http://gateway-server/notification/api/")
                    .build();

            webClient.post()
                    .uri("endpoint")
                    .body(CustomInserter.fromValue(addRequest))
                    .retrieve()
                    .bodyToMono(AddEndpointResponse.class)
                    .flatMap((v) -> {
                        if (v.getStatus().equals("ok")) {

                            Map<String, Object> mapToken = new HashMap<>();
                            mapToken.put("userId", createdUser.getId());
                            mapToken.put("mission", "confirmAlternativeConnection");

                            TokenService tokenService = new TokenService(loginJwtSecret);
                            String token = tokenService.generateRawToken(mapToken, 30 * 60 * 1000);

                            Map<String, Object> pushRequest = new HashMap<>();
                            pushRequest.put("toUserId", createdUser.getId());
                            pushRequest.put("title", "Подтверждение регистрации");
                            String message = "Для подтверждения регистрации перейдите по ссылке: " +
                                    externalUrl + "/auth/api/users/confirm-alternative-connection?token=" + token;

                            pushRequest.put("message", message);

                            return webClient.post()
                                    .uri("notification/push")
                                    .body(CustomInserter.fromValue(pushRequest))
                                    .retrieve()
                                    .bodyToMono(String.class)
                                    .map((response) -> Mono.empty());
                        } else {
                            log.warn("The user transmitted incorrect alternative communication data: "
                                    + v.getError());
                            return Mono.empty();
                        }
                    })
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe();


            TokenService tokenService = new TokenService(loginJwtSecret);

            String token = tokenService.generateRawToken(Map.of("userId", createdUser.getId()),
                    7L * 24 * 60 * 60 * 1000);

            return Result.ok(Pair.of(token, createdUser));
        } else {
            return Result.err("The user already exists");
        }
    }

    public Result<String, String> confirmAlternativeConnection(String token) {
        Map<String, Claim> json;
        try {
            TokenService tokenService = new TokenService(loginJwtSecret);
            json = tokenService.validateRawToken(token);
        } catch (TokenExpiredException e) {
            return Result.err("The token has expired");
        } catch (JWTVerificationException e) {
            return Result.err("The token is incorrect");
        }

        Optional<MyUser> user = userRepository.findById(json.get("userId").asInt());

        if (user.isEmpty() || !json.containsKey("mission")
                || !json.get("mission").asString().equals("confirmAlternativeConnection")) {
            return Result.err("The token is incorrect");
        }
        user.get().setHasAlternativeConnection(true);
        userRepository.save(user.get());

        return Result.ok("ok");
    }

    public Result<String, String> createInternalUser(PureUser user, List<Role> roles) {
        String hashedPassword = encoder.encode(user.password);

        MyUser foundUser = userRepository.findUserByLogin(user.login);

        if (foundUser == null) {
            MyUser createdUser = userRepository.save(MyUser.builder()
                    .login(user.login)
                    .hashedPassword(hashedPassword)
                    .roles(roles)
                    .hasAlternativeConnection(true)
                    .build()
            );

            TokenService tokenService = new TokenService(loginJwtSecret);

            return Result.ok(tokenService.generateRawToken(Map.of("userId", createdUser.getId()),
                    7L * 24 * 60 * 60 * 1000));
        } else {
            return Result.err("The user already exists");
        }
    }

    @Override
    public Result<Pair<String, MyUser>, String> login(PureUser user) {
        MyUser foundUser = userRepository.findUserByLogin(user.login);

        if (foundUser != null) {
            boolean res = encoder.matches(user.password, foundUser.getHashedPassword());

            if (res) {
                TokenService tokenService = new TokenService(loginJwtSecret);

                String token = tokenService.generateRawToken(Map.of("userId", foundUser.getId()),
                        7L * 24 * 60 * 60 * 1000);

                return Result.ok(Pair.of(token, foundUser));
            } else {
                return Result.err("The password is incorrect");
            }
        }

        return Result.err("The user does not exist");
    }

    @Override
    public Result<Pair<Integer, Boolean>, String> checkTokenAndUserId(String token) {
        Map<String, Claim> json;
        try {
            TokenService tokenService = new TokenService(loginJwtSecret);

            json = tokenService.validateRawToken(token);

        } catch (TokenExpiredException e) {
            return Result.err("The token has expired");

        } catch (JWTVerificationException e) {
            return Result.err("The token is incorrect");
        }

        Optional<MyUser> myUser = userRepository.findById(json.get("userId").asInt());
        if (myUser.isPresent()) {
            return Result.ok(Pair.of(myUser.get().getId(), myUser.get().isHasAlternativeConnection()));
        } else {
            return Result.err("The token is incorrect");
        }
    }

    private Result<List<Role>, String> checkRole(String accessToken, Role... roles) {
        Result<Pair<Integer, Boolean>, String> token = checkTokenAndUserId(accessToken);

        if (token.type == Result.Val.Err) {
            return Result.err(token.err);
        }

        Optional<MyUser> seeker = userRepository.findById(token.ok.getFirst());
        if (seeker.isPresent()) {
            List<Role> rolesFound = new ArrayList<>();

            for (Role role : roles) {
                if (seeker.get().getRoles().contains(role)) {
                    rolesFound.add(role);
                }
            }
            if (rolesFound.size() != 0) {
                return Result.ok(rolesFound);
            } else {
                return Result.err("The user does not have the necessary roles");
            }
        }

        return Result.err("The requesting user was not found");
    }

    @Override
    public Result<List<Role>, String> getRoles(String accessToken, Integer userId) {
        Result<List<Role>, String> res = checkRole(accessToken, Role.OTHER_SERVICE, Role.ADMIN);

        if (res.type == Result.Val.Ok) {
            Optional<MyUser> foundUser = userRepository.findById(userId);

            if (foundUser.isPresent()) {
                return Result.ok(foundUser.get().getRoles());
            } else {
                return Result.err("The required user was not found");
            }
        }

        return Result.err(res.err);
    }

    @Override
    public Result<ShortInfoUser, String> getRoles(String accessToken, String userToken) {
        Result<List<Role>, String> res = checkRole(accessToken, Role.OTHER_SERVICE, Role.ADMIN);

        if (res.type == Result.Val.Ok) {

            Result<Pair<Integer, Boolean>, String> user = checkTokenAndUserId(userToken);

            if (user.type == Result.Val.Ok) {
                MyUser foundUser = userRepository.getReferenceById(user.ok.getFirst());

                return Result.ok(ShortInfoUser.builder()
                        .id(user.ok.getFirst())
                        .roles(foundUser.getRoles())
                        .hasAlternativeConnection(user.ok.getSecond())
                        .build());
            }


            return Result.err("the user token is invalid");
        } else {
            return Result.err(res.err);
        }
    }

    @Override
    public Result<Integer, String> removeRole(String accessToken, Integer userId, Role role) {
        Result<List<Role>, String> res = checkRole(accessToken, Role.OTHER_SERVICE, Role.ADMIN);

        if (res.type == Result.Val.Ok) {
            try {
                MyUser foundUser = userRepository.getReferenceById(userId);
                foundUser.getRoles().remove(role);
                userRepository.save(foundUser);

                return Result.ok(foundUser.getId());

            } catch (EntityNotFoundException e) {
                return Result.err("The required user was not found");

            }
        }

        return Result.err(res.err);
    }

    @Override
    public Result<Integer, String> addRole(String accessToken, Integer userId, Role role) {
        Result<List<Role>, String> res = checkRole(accessToken, Role.OTHER_SERVICE, Role.ADMIN);

        if (res.type == Result.Val.Ok) {
            try {
                MyUser foundUser = userRepository.getReferenceById(userId);
                foundUser.getRoles().add(role);
                userRepository.save(foundUser);

                return Result.ok(foundUser.getId());

            } catch (EntityNotFoundException e) {
                return Result.err("The required user was not found");

            }
        }

        return Result.err(res.err);
    }

    @Override
    public Result<List<String>, String> getAllUsers(String accessToken) {
        Result<List<Role>, String> res = checkRole(accessToken);

        if (res.type == Result.Val.Ok) {
            return Result.ok(userRepository.findAll().stream().map(MyUser::getLogin).collect(Collectors.toList()));
        }

        return Result.err(res.err);
    }

    @Override
    public Result<String, String> changePassword(String accessToken, String newPassword) {
        Result<Pair<Integer, Boolean>, String> token = checkTokenAndUserId(accessToken);

        if (token.type == Result.Val.Err) {
            return Result.err(token.err);
        }

        try {
            MyUser foundUser = userRepository.getReferenceById(token.ok.getFirst());
            foundUser.setHashedPassword(encoder.encode(newPassword));
            userRepository.save(foundUser);

            return Result.ok("ok");

        } catch (EntityNotFoundException e) {
            return Result.err("The required user was not found");
        }
    }

    @Override
    public Result<String, String> checkId(String accessToken, Integer id) {
        Result<Pair<Integer, Boolean>, String> token = checkTokenAndUserId(accessToken);

        if (token.type == Result.Val.Err) {
            return Result.err(token.err);
        }

        Optional<MyUser> foundUser = userRepository.findById(id);

        if (foundUser.isPresent()) {
            return Result.ok("ok");
        } else {
            return Result.err("The required user was not found");
        }
    }
}
