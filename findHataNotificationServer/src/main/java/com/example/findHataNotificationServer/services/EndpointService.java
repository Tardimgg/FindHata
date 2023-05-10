package com.example.findHataNotificationServer.services;

//import com.example.findHataNotificationServer.Result;
import com.example.Result;
import com.example.findHataNotificationServer.entities.Endpoint;
import com.example.findHataNotificationServer.entities.Role;

import java.util.List;

public interface EndpointService {

    Result<String, String> addAllEndpoints(List<Role> from, Integer fromId, Integer userId, List<Endpoint> endpoints);

}
