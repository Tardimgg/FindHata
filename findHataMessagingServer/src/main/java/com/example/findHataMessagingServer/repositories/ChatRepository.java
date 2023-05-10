package com.example.findHataMessagingServer.repositories;

import com.example.findHataMessagingServer.entities.Chat;
import com.example.findHataMessagingServer.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, String>, JpaSpecificationExecutor<Chat> {

    Chat save(Chat chat);
}
