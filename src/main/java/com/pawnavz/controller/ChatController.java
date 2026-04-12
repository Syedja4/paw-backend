package com.pawnavz.controller;

import com.pawnavz.entity.ChatMessage;
import com.pawnavz.repository.ChatMessageRepository;
import com.pawnavz.repository.UserRepository;
import com.pawnavz.security.JwtUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Chat")
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final JwtUtil jwtUtil;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatPayload payload,
                            @Header("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.replace("Bearer ", ""));
        ChatMessage message = ChatMessage.builder()
                .sessionId(payload.getSessionId())
                .user(userRepository.getReferenceById(userId))
                .senderType(ChatMessage.SenderType.USER)
                .content(payload.getContent())
                .messageType(ChatMessage.MessageType.TEXT)
                .build();
        ChatMessage saved = chatMessageRepository.save(message);
        messagingTemplate.convertAndSend("/topic/chat/" + payload.getSessionId(), toResponse(saved));
    }

    @GetMapping("/api/v1/chat/{sessionId}/history")
    public List<ChatMessageResponse> getHistory(@PathVariable String sessionId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "50") int size) {
        return chatMessageRepository
                .findBySessionIdOrderByCreatedAtAsc(sessionId, PageRequest.of(page, size))
                .map(this::toResponse).getContent();
    }

    private ChatMessageResponse toResponse(ChatMessage msg) {
        return ChatMessageResponse.builder()
                .id(msg.getId()).sessionId(msg.getSessionId())
                .content(msg.getContent()).senderType(msg.getSenderType().name())
                .messageType(msg.getMessageType().name()).createdAt(msg.getCreatedAt()).build();
    }

    @Data
    public static class ChatPayload {
        private String sessionId;
        private String content;
    }

    @Data
    @Builder
    public static class ChatMessageResponse {
        private String id;
        private String sessionId;
        private String content;
        private String senderType;
        private String messageType;
        private LocalDateTime createdAt;
    }
}
