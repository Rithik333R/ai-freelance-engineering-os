package com.freelance.os.ai.action.controller;

import com.freelance.os.ai.action.dto.AiActionExecutionRequest;
import com.freelance.os.ai.action.dto.AiActionExecutionResult;
import com.freelance.os.ai.action.dto.AiActionProposal;
import com.freelance.os.ai.action.service.AiActionExecutor;
import com.freelance.os.ai.action.service.AiActionExtractor;
import com.freelance.os.common.ApiResponse;
import com.freelance.os.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/ai/actions")
@RequiredArgsConstructor
public class AiActionController {

    private final AiActionExtractor aiActionExtractor;
    private final AiActionExecutor aiActionExecutor;

    @PostMapping("/extract")
    public ResponseEntity<ApiResponse<AiActionProposal>> extractAction(
            @RequestBody Map<String, String> requestBody) {
        String message = requestBody != null ? requestBody.get("message") : null;
        Optional<AiActionProposal> proposal = aiActionExtractor.extractActionProposal(message);
        return ResponseEntity.ok(ApiResponse.success(
                proposal.isPresent() ? "Action intent extracted successfully" : "No action intent detected",
                proposal.orElse(null)
        ));
    }

    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<AiActionExecutionResult>> executeAction(
            @RequestBody @Valid AiActionExecutionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        AiActionExecutionResult result = aiActionExecutor.executeAction(request, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(result.getMessage(), result));
    }
}
