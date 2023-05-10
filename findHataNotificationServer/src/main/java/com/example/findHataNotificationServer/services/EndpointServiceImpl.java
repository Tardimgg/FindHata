package com.example.findHataNotificationServer.services;

import com.example.Result;
import com.example.findHataNotificationServer.entities.Endpoint;
import com.example.findHataNotificationServer.entities.Role;
import com.example.findHataNotificationServer.entities.UserEndpoints;
import com.example.findHataNotificationServer.repositories.EndpointRepository;
import com.example.findHataNotificationServer.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EndpointServiceImpl implements EndpointService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    EndpointRepository endpointRepository;

    @Override
    public Result<String, String> addAllEndpoints(List<Role> from, Integer fromId, Integer userId, List<Endpoint> endpoints) {
        if (!from.contains(Role.OTHER_SERVICE) && !fromId.equals(userId)) {
            return Result.err("The user does not have the right to change endpoints");
        }

        for (Endpoint endpoint: endpoints) {
            endpointRepository.save(endpoint);
        }

        UserEndpoints userEndpoints = userRepository.findUserEndpointsByUserId(userId);
        if (userEndpoints == null) {
            userEndpoints = UserEndpoints.builder()
                    .userId(userId)
                    .endpoints(endpoints)
                    .build();
        } else {
            userEndpoints.getEndpoints().addAll(endpoints);
        }

        userRepository.save(userEndpoints);

        return Result.ok("Ok");
    }

}
