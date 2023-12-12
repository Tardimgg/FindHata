package com.example.findHataProposalServer.entities;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Data
@Entity
@Table(name = "vectorized_fact")
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class VectorizedFact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NonNull
    double[] vector;
}
