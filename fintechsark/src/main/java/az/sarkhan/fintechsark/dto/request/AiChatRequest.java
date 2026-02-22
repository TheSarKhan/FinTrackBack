package az.sarkhan.fintechsark.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AiChatRequest(
        @NotBlank(message = "Message cannot be empty") String message
) {}
