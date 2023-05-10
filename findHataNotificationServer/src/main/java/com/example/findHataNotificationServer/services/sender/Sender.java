package com.example.findHataNotificationServer.services.sender;

public interface Sender {

    void send(String to, String title, String message);

}
