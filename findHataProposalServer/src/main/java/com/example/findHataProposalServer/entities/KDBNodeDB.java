package com.example.findHataProposalServer.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.Type;
import org.hibernate.type.descriptor.jdbc.VarbinaryJdbcType;

@Data
@Builder
@Entity
@Table(name = "kdb_node")
@AllArgsConstructor
@NoArgsConstructor
public class KDBNodeDB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "is_leaf")
    private boolean isLeaf;

    @Column(name = "splitting_index")
    private int splittingIndex;

    @Lob
    @Column(name = "vals")
    @JdbcType(VarbinaryJdbcType.class)
    private byte[] vals;

    @Column(name = "vals_len")
    private int valsLen;

    @Lob
    @Column(name = "vector_or_nodes_ids")
    @JdbcType(VarbinaryJdbcType.class)
    private byte[] vectorOrNodesIds;

    @Column(name = "vec_or_nodes_len")
    private int vecOrNodesLen;

}
