package com.freelance.os.ai.action;

import com.freelance.os.ai.action.dto.AiActionProposal;
import com.freelance.os.ai.action.enums.AiActionType;
import com.freelance.os.ai.action.service.AiActionExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AiActionExtractorTest {

    private AiActionExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new AiActionExtractor();
    }

    @Test
    @DisplayName("Should extract CREATE_CLIENT proposal from user prompt")
    void extractActionProposal_CreateClient() {
        String message = "Create client named 'Globex Corp' with email info@globex.com";
        Optional<AiActionProposal> proposalOpt = extractor.extractActionProposal(message);

        assertTrue(proposalOpt.isPresent());
        AiActionProposal proposal = proposalOpt.get();
        assertEquals(AiActionType.CREATE_CLIENT, proposal.getActionType());
        assertNotNull(proposal.getClientPayload());
        assertEquals("Globex Corp", proposal.getClientPayload().getCompanyName());
        assertEquals("info@globex.com", proposal.getClientPayload().getContactEmail());
    }

    @Test
    @DisplayName("Should extract CREATE_PROJECT proposal with budget from user prompt")
    void extractActionProposal_CreateProject() {
        String message = "Create project 'Mobile Redesign' with budget $15000";
        Optional<AiActionProposal> proposalOpt = extractor.extractActionProposal(message);

        assertTrue(proposalOpt.isPresent());
        AiActionProposal proposal = proposalOpt.get();
        assertEquals(AiActionType.CREATE_PROJECT, proposal.getActionType());
        assertNotNull(proposal.getProjectPayload());
        assertEquals("Mobile Redesign", proposal.getProjectPayload().getName());
        assertEquals(new BigDecimal("15000"), proposal.getProjectPayload().getBudget());
    }

    @Test
    @DisplayName("Should extract CREATE_TASK proposal from user prompt")
    void extractActionProposal_CreateTask() {
        String message = "Create task 'Setup CI/CD Pipeline'";
        Optional<AiActionProposal> proposalOpt = extractor.extractActionProposal(message);

        assertTrue(proposalOpt.isPresent());
        AiActionProposal proposal = proposalOpt.get();
        assertEquals(AiActionType.CREATE_TASK, proposal.getActionType());
        assertNotNull(proposal.getTaskPayload());
        assertEquals("Setup CI/CD Pipeline", proposal.getTaskPayload().getTitle());
    }

    @Test
    @DisplayName("Should return empty optional for conversational prompts with no action intent")
    void extractActionProposal_NoAction() {
        String message = "Which projects have upcoming deadlines this week?";
        Optional<AiActionProposal> proposalOpt = extractor.extractActionProposal(message);

        assertFalse(proposalOpt.isPresent());
    }
}
