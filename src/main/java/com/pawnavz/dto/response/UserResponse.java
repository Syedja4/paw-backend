package com.pawnavz.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String profileImageUrl;
    private String role;
    private Boolean darkModeEnabled;
    private Boolean blocked;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}