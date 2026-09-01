package com.freelance.os.ai;

import com.freelance.os.ai.dto.ConversationResponse;
import com.freelance.os.ai.dto.CreateConversationRequest;
import com.freelance.os.ai.dto.SendMessageRequest;
import com.freelance.os.ai.entity.ConversationMessage;
import com.freelance.os.ai.service.ConversationService;
import com.freelance.os.auth.AuthService;
import com.freelance.os.auth.dto.AuthResponse;
import com.freelance.os.auth.dto.RegisterRequest;
import com.freelance.os.common.exception.AiServiceException;
import com.freelance.os.user.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiService aiService;

    private String user1Token;
    private UUID user1Id;

    private String user2Token;
    private UUID user2Id;

    @BeforeEach
    void setUp() {
        AuthResponse auth1 = authService.register(RegisterRequest.builder()
                .email("conv_user1@example.com")
                .password("Password123!")
                .fullName("Conv User 1")
                .role(Role.ROLE_FREELANCER)
                .build());
        user1Token = auth1.getAccessToken();
        user1Id = auth1.getUser().getId();

        AuthResponse auth2 = authService.register(RegisterRequest.builder()
                .email("conv_user2@example.com")
                .password("Password123!")
                .fullName("Conv User 2")
                .role(Role.ROLE_FREELANCER)
                .build());
        user2Token = auth2.getAccessToken();
        user2Id = auth2.getUser().getId();
    }

    @Test
    void testCreateConversation_Success() throws Exception {
        CreateConversationRequest req = CreateConversationRequest.builder()
                .title("Project Planning")
                .build();

        mockMvc.perform(post("/ai/conversations")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Project Planning"))
                .andExpect(jsonPath("$.data.id").exists());
    }

    @Test
    void testCreateConversation_BlankTitle_Returns400() throws Exception {
        CreateConversationRequest req = CreateConversationRequest.builder()
                .title("   ")
                .build();

        mockMvc.perform(post("/ai/conversations")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGetUserConversations_Success() throws Exception {
        conversationService.createConversation(CreateConversationRequest.builder().title("Conv 1").build(), user1Id);
        conversationService.createConversation(CreateConversationRequest.builder().title("Conv 2").build(), user1Id);

        mockMvc.perform(get("/ai/conversations")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testGetConversation_Success() throws Exception {
        ConversationResponse conv = conversationService.createConversation(CreateConversationRequest.builder().title("Conv 1").build(), user1Id);

        mockMvc.perform(get("/ai/conversations/" + conv.getId())
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(conv.getId().toString()))
                .andExpect(jsonPath("$.data.title").value("Conv 1"));
    }

    @Test
    void testSendMessage_Success() throws Exception {
        ConversationResponse conv = conversationService.createConversation(CreateConversationRequest.builder().title("Architecture Inquiry").build(), user1Id);

        when(aiService.chatWithHistory(any(List.class), eq("How do I structure the backend?"), eq(user1Id)))
                .thenReturn("Use a multi-layered Spring Boot architecture.");

        SendMessageRequest msgReq = SendMessageRequest.builder()
                .message("How do I structure the backend?")
                .build();

        mockMvc.perform(post("/ai/conversations/" + conv.getId() + "/messages")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msgReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("ASSISTANT"))
                .andExpect(jsonPath("$.data.content").value("Use a multi-layered Spring Boot architecture."));
    }

    @Test
    void testSendMessage_BlankMessage_Returns400() throws Exception {
        ConversationResponse conv = conversationService.createConversation(CreateConversationRequest.builder().title("Test").build(), user1Id);

        SendMessageRequest msgReq = SendMessageRequest.builder()
                .message("   ")
                .build();

        mockMvc.perform(post("/ai/conversations/" + conv.getId() + "/messages")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msgReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testDeleteConversation_Success() throws Exception {
        ConversationResponse conv = conversationService.createConversation(CreateConversationRequest.builder().title("To Delete").build(), user1Id);

        mockMvc.perform(delete("/ai/conversations/" + conv.getId())
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/ai/conversations/" + conv.getId())
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUserIsolation_UserACannotAccessUserBConversation() throws Exception {
        ConversationResponse convUser1 = conversationService.createConversation(CreateConversationRequest.builder().title("User 1 Private Conv").build(), user1Id);

        // User 2 attempts GET
        mockMvc.perform(get("/ai/conversations/" + convUser1.getId())
                        .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUserIsolation_UserACannotSendMessageToUserBConversation() throws Exception {
        ConversationResponse convUser1 = conversationService.createConversation(CreateConversationRequest.builder().title("User 1 Private Conv").build(), user1Id);

        SendMessageRequest msgReq = SendMessageRequest.builder()
                .message("Unauthorized intrusion attempt")
                .build();

        // User 2 attempts POST message
        mockMvc.perform(post("/ai/conversations/" + convUser1.getId() + "/messages")
                        .header("Authorization", "Bearer " + user2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msgReq)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUserIsolation_UserACannotDeleteUserBConversation() throws Exception {
        ConversationResponse convUser1 = conversationService.createConversation(CreateConversationRequest.builder().title("User 1 Private Conv").build(), user1Id);

        // User 2 attempts DELETE
        mockMvc.perform(delete("/ai/conversations/" + convUser1.getId())
                        .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUnauthenticatedRequest_Returns401() throws Exception {
        mockMvc.perform(get("/ai/conversations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testSendMessage_GeminiFailure_Returns503() throws Exception {
        ConversationResponse conv = conversationService.createConversation(CreateConversationRequest.builder().title("Gemini Fail Conv").build(), user1Id);

        when(aiService.chatWithHistory(any(List.class), eq("Test fail"), eq(user1Id)))
                .thenThrow(new AiServiceException("Failed to generate AI response. Please try again later."));

        SendMessageRequest msgReq = SendMessageRequest.builder()
                .message("Test fail")
                .build();

        mockMvc.perform(post("/ai/conversations/" + conv.getId() + "/messages")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msgReq)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to generate AI response. Please try again later."));
    }
}
