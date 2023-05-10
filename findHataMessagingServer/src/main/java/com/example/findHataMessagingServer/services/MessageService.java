package com.example.findHataMessagingServer.services;

import com.example.Result;
import com.example.findHataMessagingServer.entities.Message;
import com.example.findHataMessagingServer.entities.Role;
import com.example.findHataMessagingServer.entities.responses.RecentPostsResponse;

import java.util.List;

public interface MessageService {

    Result<String, String> send(Integer proposalId, Integer fromId, Integer toId, String message);

    Result<List<Message>, String> getCorrespondence(Integer proposalId, Integer questionerId, Integer withId);

    Result<List<Message>, String> getCorrespondence(Integer proposalId, Integer questionerId, Integer withId, Long afterTime);

    Result<List<RecentPostsResponse>, String> getRecentPosts(Integer questionerId);

    Result<List<Message>, String> getCorrespondence(List<Role> roles, Integer proposalId, Integer firstId, Integer secondId);
}
