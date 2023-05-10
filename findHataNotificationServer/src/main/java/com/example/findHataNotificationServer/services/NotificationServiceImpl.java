package com.example.findHataNotificationServer.services;

//import com.example.findHataNotificationServer.Result;
import com.example.Result;
import com.example.findHataNotificationServer.entities.Endpoint;
import com.example.findHataNotificationServer.entities.Role;
import com.example.findHataNotificationServer.entities.UserEndpoints;
import com.example.findHataNotificationServer.repositories.UserRepository;
import com.example.findHataNotificationServer.services.sender.SenderStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    SenderStore senderStore;

    @Autowired
    UserRepository userRepository;


    @Override
    public Result<String, String> sendMessage(List<Role> fromRole, Integer to, String title, String message) {
        if (!fromRole.contains(Role.OTHER_SERVICE)) {
            return Result.err("The user does not have the right to send messages");
        }

        UserEndpoints userEndpoints = userRepository.findUserEndpointsByUserId(to);

        if (userEndpoints != null) {

            AtomicBoolean isShipped = new AtomicBoolean(false);
            for (Endpoint endpoint : userEndpoints.getEndpoints()) {
                try {
                    senderStore.getSender(endpoint.getTypeCommunication()).ifPresent((v) -> {
                        v.send(endpoint.getUrl(), title, message);
                        isShipped.set(true);
                    });
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }

            if (!isShipped.get()) {
                return Result.err("No driver found to send the message");
            } else {
                return Result.ok("in process");
            }
        }
        return Result.err("Unknown user");
    }
}
