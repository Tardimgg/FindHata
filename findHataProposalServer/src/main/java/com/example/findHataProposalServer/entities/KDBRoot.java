package com.example.findHataProposalServer.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Entity
@Table(name = "kdb_root")
@NoArgsConstructor
public class KDBRoot {

    public KDBRoot(KDBNodeDB kdbNodeDB) {
        this.kdbNodeDB = kdbNodeDB;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @OneToOne
    KDBNodeDB kdbNodeDB;
}
