package com.example.findHataNotificationServer.services.sender;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class SenderStore {

    private Map<SenderType, Sender> map = new HashMap<>();

    public Optional<Sender> getSender(SenderType senderType) {
        return Optional.ofNullable(map.get(senderType));
    }

    public void register(SenderType senderType, Sender sender) {
        map.put(senderType, sender);
    }

}
