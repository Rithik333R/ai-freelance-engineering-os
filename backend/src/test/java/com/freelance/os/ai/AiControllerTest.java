package com.freelance.os.ai;

import com.freelance.os.ai.dto.AiResponse;
import com.freelance.os.auth.AuthService;
import com.freelance.os.auth.dto.AuthResponse;
import com.freelance.os.auth.dto.RegisterRequest;
import com.freelance.os.common.exception.AiServiceException;
import com.freelance.os.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @MockBean
    private AiService aiService;

    private String jwtToken;
    private UUID user1Id;

    @BeforeEach
    void setUp() {
        AuthResponse authResponse = authService.register(RegisterRequest.builder()
                .email("aicontrollertest@example.com")
                .password("Password123!")
                .fullName("AI Controller Tester")
                .role(Role.ROLE_FREELANCER)
                .build());
        jwtToken = authResponse.getAccessToken();
        user1Id = authResponse.getUser().getId();
    }

    // 1. Blank message validation error test (400 Bad Request)
    @Test
    void testChat_BlankMessage_Returns400BadRequest() throws Exception {
        String requestJson = """
                {
                    "message": "   "
                }
                """;

        mockMvc.perform(post("/ai/chat")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.message").value("Message is required and must not be blank"));
    }

    // 2. Null message validation error test (400 Bad Request)
    @Test
    void testChat_NullMessage_Returns400BadRequest() throws Exception {
        String requestJson = """
                {
                    "message": null
                }
                """;

        mockMvc.perform(post("/ai/chat")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.message").value("Message is required and must not be blank"));
    }

    // 3. Valid authenticated request (200 OK with ApiResponse<AiResponse>)
    @Test
    void testChat_ValidAuthenticatedRequest_Returns200WithApiResponse() throws Exception {
        when(aiService.chat(eq("Hello AI"), any(UUID.class)))
                .thenReturn(AiResponse.builder().response("AI System Online").build());

        String requestJson = """
                {
                    "message": "Hello AI"
                }
                """;

        mockMvc.perform(post("/ai/chat")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("AI response generated successfully"))
                .andExpect(jsonPath("$.data.response").value("AI System Online"));
    }

    // 4. Gemini failure handled gracefully (503 Service Unavailable)
    @Test
    void testChat_GeminiFailure_ReturnsControlledErrorResponse() throws Exception {
        when(aiService.chat(eq("Explain project status"), any(UUID.class)))
                .thenThrow(new AiServiceException("Failed to generate AI response. Please try again later."));

        String requestJson = """
                {
                    "message": "Explain project status"
                }
                """;

        mockMvc.perform(post("/ai/chat")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to generate AI response. Please try again later."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    // 5. Missing API key test (503 Service Unavailable with clean message)
    @Test
    void testChat_MissingApiKey_Returns503CleanError() throws Exception {
        when(aiService.chat(eq("Test missing key"), any(UUID.class)))
                .thenThrow(new AiServiceException("Gemini API key is not configured. AI service is unavailable."));

        String requestJson = """
                {
                    "message": "Test missing key"
                }
                """;

        mockMvc.perform(post("/ai/chat")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Gemini API key is not configured. AI service is unavailable."));
    }

    // 6. Direct AiService test for missing key exception throwing
    @Test
    void testAiService_DirectMissingApiKey_ThrowsCleanAiServiceException() {
        AiContextBuilder mockContextBuilder = org.mockito.Mockito.mock(AiContextBuilder.class);
        AiService serviceWithNoKey = new AiService("", mockContextBuilder);

        AiServiceException ex = assertThrows(AiServiceException.class, () ->
                serviceWithNoKey.chat("Test prompt", UUID.randomUUID())
        );

        assertEquals("Gemini API key is not configured. AI service is unavailable.", ex.getMessage());
        assertFalse(ex.getMessage().contains("stackTrace"));
        assertFalse(ex.getMessage().contains("password"));
    }
}
