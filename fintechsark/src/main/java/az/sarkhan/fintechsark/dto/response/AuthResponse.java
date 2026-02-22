package az.sarkhan.fintechsark.dto.response;

public record AuthResponse(
        String token,
        String name,
        String email,
        Long userId
) {}
