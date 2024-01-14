package com.example.findHataProposalServer.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.VarbinaryJdbcType;

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

//    @NonNull
//    double[] vector;

    @Lob
    @Column(name = "vector_or_nodes_ids")
    @JdbcType(VarbinaryJdbcType.class)
    @NonNull
    private byte[] vector;

}
