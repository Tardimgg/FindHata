package com.example.findHataMessagingServer.entities;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@Entity
@Table(name = "messages")
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NonNull
    @Column(name = "from_id")
    private Integer fromId;

    @NonNull
    @Column(name = "to_id")
    private Integer toId;

    @NonNull
    @Column(name = "time")
    private Long time;

    @NonNull
    @Column(name = "message")
    private String message;

    @NonNull
    @Column(name = "read")
    private boolean read = false;
}
