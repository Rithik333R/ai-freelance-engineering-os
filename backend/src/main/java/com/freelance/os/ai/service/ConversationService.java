package com.freelance.os.ai.service;

import com.freelance.os.ai.AiService;
import com.freelance.os.ai.dto.*;
import com.freelance.os.ai.entity.Conversation;
import com.freelance.os.ai.entity.ConversationMessage;
import com.freelance.os.ai.entity.MessageRole;
import com.freelance.os.ai.repository.ConversationMessageRepository;
import com.freelance.os.ai.repository.ConversationRepository;
import com.freelance.os.common.exception.ResourceNotFoundException;
import com.freelance.os.user.User;
import com.freelance.os.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final UserRepository userRepository;
    private final AiService aiService;

    public ConversationResponse createConversation(CreateConversationRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Conversation conversation = Conversation.builder()
                .user(user)
                .title(request.getTitle().trim())
                .build();

        Conversation saved = conversationRepository.save(conversation);
        return mapToConversationResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations(UUID userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(this::mapToConversationResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversation(UUID conversationId, UUID userId) {
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        return mapToConversationResponse(conversation);
    }

    public void deleteConversation(UUID conversationId, UUID userId) {
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        conversationRepository.delete(conversation);
    }

    public ConversationMessageResponse sendMessage(UUID conversationId, SendMessageRequest request, UUID userId) {
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        String userMessageContent = request.getMessage().trim();

        // 1. Get existing history before adding current message
        List<ConversationMessage> history = conversationMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        // 2. Persist USER message
        ConversationMessage userMsg = ConversationMessage.builder()
                .conversation(conversation)
                .role(MessageRole.USER)
                .content(userMessageContent)
                .build();
        conversationMessageRepository.save(userMsg);

        // 3. Call Gemini with history, user message, and user context
        String assistantResponseText = aiService.chatWithHistory(history, userMessageContent, userId);

        // 4. Persist ASSISTANT message
        ConversationMessage assistantMsg = ConversationMessage.builder()
                .conversation(conversation)
                .role(MessageRole.ASSISTANT)
                .content(assistantResponseText)
                .build();
        ConversationMessage savedAssistantMsg = conversationMessageRepository.save(assistantMsg);

        // Touch conversation updated timestamp
        conversationRepository.save(conversation);

        return mapToMessageResponse(savedAssistantMsg);
    }

    private ConversationResponse mapToConversationResponse(Conversation c) {
        List<ConversationMessageResponse> messageResponses = c.getMessages() != null ?
                c.getMessages().stream().map(this::mapToMessageResponse).collect(Collectors.toList()) :
                List.of();

        return ConversationResponse.builder()
                .id(c.getId())
                .title(c.getTitle())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .messages(messageResponses)
                .build();
    }

    private ConversationMessageResponse mapToMessageResponse(ConversationMessage msg) {
        return ConversationMessageResponse.builder()
                .id(msg.getId())
                .conversationId(msg.getConversation().getId())
                .role(msg.getRole())
                .content(msg.getContent())
                .createdAt(msg.getCreatedAt())
                .build();
    }
}
