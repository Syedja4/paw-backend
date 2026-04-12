package com.pawnavz.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private String id;
    private String type;
    private String title;
    private String body;
    private String referenceId;
    private String referenceType;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
