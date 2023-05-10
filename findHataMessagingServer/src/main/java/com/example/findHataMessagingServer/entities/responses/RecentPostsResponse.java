package com.example.findHataMessagingServer.entities.responses;


import com.example.findHataMessagingServer.entities.Message;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class RecentPostsResponse {

    @NonNull
    Message message;

    @NonNull
    Integer proposalId;

}
