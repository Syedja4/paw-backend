package com.pawnavz.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Admin-only shop creation. The owner must be an existing User that already holds ROLE_SHOP;
 * no new authentication account is created here (single auth flow via /api/v1/auth/login).
 */
@Data
public class AdminCreateShopRequest {

    @NotBlank
    private String ownerUserId;

    @NotBlank
    private String shopName;

    @NotBlank
    private String ownerName;

    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be a 10-digit number")
    private String phone;

    @Email
    private String email;

    private String gstNumber;

    @NotBlank
    private String address;

    private Double latitude;

    private Double longitude;
}
