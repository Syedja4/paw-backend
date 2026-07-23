package com.pawnavz.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class User extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String phone;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Builder.Default
    private Boolean darkModeEnabled = false;

    @Builder.Default
    private Boolean blocked = false;

    @Column(columnDefinition = "TEXT")
    private String refreshToken;
    // DRIVER is retained only for backward compatibility with existing rows/tokens;
    // the driver module has been removed. SHOP is used by shop-partner owners.
    public enum Role { USER, ADMIN, DRIVER, SHOP }
}
