package com.freelance.os.ai;

import com.freelance.os.ai.dto.*;
import com.freelance.os.ai.entity.Conversation;
import com.freelance.os.ai.entity.ConversationMessage;
import com.freelance.os.ai.entity.MessageRole;
import com.freelance.os.ai.repository.ConversationMessageRepository;
import com.freelance.os.ai.repository.ConversationRepository;
import com.freelance.os.ai.service.ConversationService;
import com.freelance.os.common.exception.ResourceNotFoundException;
import com.freelance.os.user.User;
import com.freelance.os.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationMessageRepository conversationMessageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AiService aiService;

    @InjectMocks
    private ConversationService conversationService;

    private UUID userId;
    private User mockUser;
    private UUID conversationId;
    private Conversation mockConversation;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUser = User.builder().email("test@example.com").fullName("Test User").build();
        mockUser.setId(userId);

        conversationId = UUID.randomUUID();
        mockConversation = Conversation.builder()
                .user(mockUser)
                .title("Test Conversation")
                .messages(new ArrayList<>())
                .build();
        mockConversation.setId(conversationId);
    }

    @Test
    void testCreateConversation_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(mockConversation);

        CreateConversationRequest request = CreateConversationRequest.builder().title("Test Conversation").build();
        ConversationResponse response = conversationService.createConversation(request, userId);

        assertNotNull(response);
        assertEquals("Test Conversation", response.getTitle());
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void testGetConversation_OwnershipVerified() {
        when(conversationRepository.findByIdAndUserId(conversationId, userId)).thenReturn(Optional.of(mockConversation));

        ConversationResponse response = conversationService.getConversation(conversationId, userId);

        assertNotNull(response);
        assertEquals(conversationId, response.getId());
    }

    @Test
    void testGetConversation_NotOwned_ThrowsResourceNotFoundException() {
        when(conversationRepository.findByIdAndUserId(conversationId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                conversationService.getConversation(conversationId, userId)
        );
    }

    @Test
    void testSendMessage_PersistsUserAndAssistantMessages() {
        when(conversationRepository.findByIdAndUserId(conversationId, userId)).thenReturn(Optional.of(mockConversation));
        when(conversationMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)).thenReturn(new ArrayList<>());
        when(aiService.chatWithHistory(any(List.class), eq("What are my active projects?"), eq(userId)))
                .thenReturn("You have 2 active projects: E-Commerce Portal and Client Dashboard.");

        ConversationMessage mockUserMsg = ConversationMessage.builder().conversation(mockConversation).role(MessageRole.USER).content("What are my active projects?").build();
        mockUserMsg.setId(UUID.randomUUID());

        ConversationMessage mockAssistantMsg = ConversationMessage.builder().conversation(mockConversation).role(MessageRole.ASSISTANT).content("You have 2 active projects: E-Commerce Portal and Client Dashboard.").build();
        mockAssistantMsg.setId(UUID.randomUUID());

        when(conversationMessageRepository.save(any(ConversationMessage.class)))
                .thenReturn(mockUserMsg)
                .thenReturn(mockAssistantMsg);

        SendMessageRequest request = SendMessageRequest.builder().message("What are my active projects?").build();
        ConversationMessageResponse response = conversationService.sendMessage(conversationId, request, userId);

        assertNotNull(response);
        assertEquals(MessageRole.ASSISTANT, response.getRole());
        assertEquals("You have 2 active projects: E-Commerce Portal and Client Dashboard.", response.getContent());

        verify(conversationMessageRepository, times(2)).save(any(ConversationMessage.class));
        verify(conversationRepository).save(mockConversation);
    }
}
