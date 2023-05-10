package com.example.findHataNotificationServer.services;

import com.example.Result;
import com.example.findHataNotificationServer.entities.Role;

import java.util.List;

public interface NotificationService {

    Result<String, String> sendMessage(List<Role> fromRoles, Integer to, String title, String message);
}
