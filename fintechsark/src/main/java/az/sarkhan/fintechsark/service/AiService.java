package az.sarkhan.fintechsark.service;

import az.sarkhan.fintechsark.dto.response.AiChatResponse;

public interface AiService {
    AiChatResponse chat(String userMessage);
    AiChatResponse analyzeCurrentMonth();
}
