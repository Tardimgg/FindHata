package com.example.findHataProposalServer.services;

import com.example.findHataProposalServer.entities.ProposalDB;
import com.example.findHataProposalServer.repositories.ProposalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProposalsServiceImplTest {

    @Mock
    private ProposalRepository proposalRepository;

    @Test
    void getAllWithKeyword() {
        ProposalDB f = ProposalDB.builder()
                .title("hii")
                .price(10)
                .description("hihi")
                .images(List.of())
                .location("hi")
                .ownerId(1)
                .id(10)
                .build();

        ProposalDB s = ProposalDB.builder()
                .title("hi")
                .price(10)
                .description("hihi")
                .images(List.of())
                .location("hi")
                .ownerId(2)
                .id(0)
                .build();

        List<ProposalDB> response = new ArrayList<>();
        response.add(f);
        response.add(s);


        Mockito.when(proposalRepository.findAll(Mockito.any(Specification.class))).thenReturn(response);

        ProposalsServiceImpl service = new ProposalsServiceImpl();
        service.proposalRepository = proposalRepository;

        assertEquals(service.getAllWithKeywordKMP("hii").get(0).getId(), 10);

    }
}