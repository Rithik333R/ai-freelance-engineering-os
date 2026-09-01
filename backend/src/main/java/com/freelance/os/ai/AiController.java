package com.freelance.os.ai;

import com.freelance.os.ai.dto.AiResponse;
import com.freelance.os.common.ApiResponse;
import com.freelance.os.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AiResponse>> chat(@RequestBody @Valid AiRequest request,
                                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getId();
        AiResponse response = aiService.chat(request.message(), userId);
        return ResponseEntity.ok(ApiResponse.success("AI response generated successfully", response));
    }
}
