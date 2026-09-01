package com.freelance.os.ai.dto;

import com.freelance.os.ai.entity.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMessageResponse {
    private UUID id;
    private UUID conversationId;
    private MessageRole role;
    private String content;
    private LocalDateTime createdAt;
}
