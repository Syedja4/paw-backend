package com.pawnavz.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_messages")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatMessage extends BaseEntity {

    @Column(nullable = false)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SenderType senderType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    @Builder.Default
    private Boolean isRead = false;

    public enum SenderType { USER, SUPPORT }
    public enum MessageType { TEXT, IMAGE, FILE }
}
