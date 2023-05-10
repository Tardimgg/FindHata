package com.example.findHataProposalServer.services;

import com.example.findHataProposalServer.entities.ProposalBD;
import com.example.findHataProposalServer.entities.ShortInfoProposal;
import com.example.findHataProposalServer.repositories.ProposalRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalMatchers;
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
        ProposalBD f = ProposalBD.builder()
                .title("hii")
                .price(10)
                .description("hihi")
                .images(List.of())
                .location("hi")
                .ownerId(1)
                .id(10)
                .build();

        ProposalBD s = ProposalBD.builder()
                .title("hi")
                .price(10)
                .description("hihi")
                .images(List.of())
                .location("hi")
                .ownerId(2)
                .id(0)
                .build();

        List<ProposalBD> response = new ArrayList<>();
        response.add(f);
        response.add(s);


        Mockito.when(proposalRepository.findAll(Mockito.any(Specification.class))).thenReturn(response);

        ProposalsServiceImpl service = new ProposalsServiceImpl();
        service.proposalRepository = proposalRepository;

        assertEquals(service.getAllWithKeyword("hii").get(0).getId(), 10);

    }
}