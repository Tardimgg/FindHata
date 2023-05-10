package com.example.findHataNotificationServer.entities;

import com.example.findHataNotificationServer.services.sender.SenderType;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@Entity
@Table(name = "endpoints")
@AllArgsConstructor
@NoArgsConstructor
public class Endpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NonNull
    @Column(name = "url")
    String url;

    @NonNull
    @Column(name = "type_communication")
    SenderType typeCommunication;

}
