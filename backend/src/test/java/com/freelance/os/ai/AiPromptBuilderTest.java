package com.freelance.os.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiPromptBuilderTest {

    private AiPromptBuilder aiPromptBuilder;

    @BeforeEach
    void setUp() {
        aiPromptBuilder = new AiPromptBuilder();
    }

    @Test
    void testBuildPrompt_ContainsSystemPersonaAndAntiHallucinationRules() {
        String prompt = aiPromptBuilder.buildPrompt("What are my deadlines?", "Projects: None");

        assertNotNull(prompt);
        assertTrue(prompt.contains("AI Freelance Engineering Operating System"));
        assertTrue(prompt.contains("GROUNDING & ACCURACY"));
        assertTrue(prompt.contains("ANTI-HALLUCINATION"));
        assertTrue(prompt.contains("MISSING DATA"));
        assertTrue(prompt.contains("SECURITY & PRIVACY"));
    }

    @Test
    void testBuildPrompt_SeparatesSystemContextAndUserQuestion() {
        String userContext = "Clients (Total: 1):\n - [Client ID: 123] Company: Acme Corp";
        String userMessage = "List all my clients";

        String prompt = aiPromptBuilder.buildPrompt(userMessage, userContext);

        assertTrue(prompt.contains("--- BEGIN AUTHENTICATED USER CONTEXT ---"));
        assertTrue(prompt.contains("Clients (Total: 1):\n - [Client ID: 123] Company: Acme Corp"));
        assertTrue(prompt.contains("--- END AUTHENTICATED USER CONTEXT ---"));
        assertTrue(prompt.contains("User Question:\nList all my clients"));
    }

    @Test
    void testBuildPrompt_HandlesNullUserContextGracefully() {
        String prompt = aiPromptBuilder.buildPrompt("Hello", null);

        assertNotNull(prompt);
        assertTrue(prompt.contains("--- BEGIN AUTHENTICATED USER CONTEXT ---"));
        assertTrue(prompt.contains("Clients: None"));
        assertTrue(prompt.contains("Projects: None"));
        assertTrue(prompt.contains("User Question:\nHello"));
    }
}
