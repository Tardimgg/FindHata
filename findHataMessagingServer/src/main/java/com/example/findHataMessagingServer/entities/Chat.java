package com.example.findHataMessagingServer.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@Entity
@Table(name = "chats")
@AllArgsConstructor
@NoArgsConstructor
public class Chat {

    @NonNull
    @Id
    @Column(name = "pfs_id", length = 50)
    private String pfsId;


    @NonNull
    @OneToMany(fetch = FetchType.EAGER)
    List<Message> messages;
}
