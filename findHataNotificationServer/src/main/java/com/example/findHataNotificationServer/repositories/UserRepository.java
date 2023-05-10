package com.example.findHataNotificationServer.repositories;

import com.example.findHataNotificationServer.entities.Endpoint;
import com.example.findHataNotificationServer.entities.UserEndpoints;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEndpoints, Integer> {

    UserEndpoints findUserEndpointsByUserId(Integer userId);

    UserEndpoints save(@NonNull UserEndpoints user);

}
