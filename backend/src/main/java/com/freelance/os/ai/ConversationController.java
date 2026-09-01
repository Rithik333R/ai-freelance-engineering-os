package com.freelance.os.ai;

import com.freelance.os.ai.dto.*;
import com.freelance.os.ai.service.ConversationService;
import com.freelance.os.common.ApiResponse;
import com.freelance.os.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ai/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(
            @RequestBody @Valid CreateConversationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ConversationResponse response = conversationService.createConversation(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Conversation created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getUserConversations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ConversationResponse> response = conversationService.getUserConversations(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Conversations retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ConversationResponse response = conversationService.getConversation(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Conversation retrieved successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        conversationService.deleteConversation(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Conversation deleted successfully", null));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<ConversationMessageResponse>> sendMessage(
            @PathVariable("id") UUID id,
            @RequestBody @Valid SendMessageRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ConversationMessageResponse response = conversationService.sendMessage(id, request, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Message processed successfully", response));
    }
}
