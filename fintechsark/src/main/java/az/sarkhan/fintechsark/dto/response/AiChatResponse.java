package az.sarkhan.fintechsark.dto.response;

import java.time.LocalDateTime;

public record AiChatResponse(
        String message,
        LocalDateTime timestamp
) {}
