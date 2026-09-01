package com.freelance.os.ai;

import jakarta.validation.constraints.NotBlank;

public record AiRequest(
        @NotBlank(message = "Message is required and must not be blank")
        String message
) {
}