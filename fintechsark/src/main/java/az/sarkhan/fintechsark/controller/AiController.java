package az.sarkhan.fintechsark.controller;

import az.sarkhan.fintechsark.dto.request.AiChatRequest;
import az.sarkhan.fintechsark.dto.response.AiChatResponse;
import az.sarkhan.fintechsark.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /** Send a chat message, get AI analysis */
    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        return ResponseEntity.ok(aiService.chat(request.message()));
    }

    /** Automatic full analysis for current month */
    @GetMapping("/analyze")
    public ResponseEntity<AiChatResponse> analyze() {
        return ResponseEntity.ok(aiService.analyzeCurrentMonth());
    }
}
