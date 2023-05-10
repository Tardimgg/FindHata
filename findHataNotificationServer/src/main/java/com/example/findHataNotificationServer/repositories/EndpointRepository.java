package com.example.findHataNotificationServer.repositories;

import com.example.findHataNotificationServer.entities.Endpoint;
import com.example.findHataNotificationServer.entities.UserEndpoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EndpointRepository extends JpaRepository<Endpoint, Integer> {

    Endpoint save(Endpoint endpoint);

}
