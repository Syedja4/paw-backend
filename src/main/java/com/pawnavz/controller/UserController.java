package com.pawnavz.controller;

import com.pawnavz.dto.request.UpdateUserRequest;
import com.pawnavz.dto.response.ApiResponse;
import com.pawnavz.dto.response.UserResponse;
import com.pawnavz.security.JwtUtil;
import com.pawnavz.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Profile")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    private String uid(String auth) { return jwtUtil.extractUserId(auth.substring(7)); }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(uid(auth))));
    }

    @PatchMapping("/me")
    @Operation(summary = "Update name and/or phone")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @RequestHeader("Authorization") String auth,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated",
                userService.updateProfile(uid(auth), request)));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload profile image")
    public ResponseEntity<ApiResponse<UserResponse>> uploadAvatar(
            @RequestHeader("Authorization") String auth,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("Profile image updated",
                userService.uploadProfileImage(uid(auth), file)));
    }

    @PatchMapping("/me/dark-mode")
    @Operation(summary = "Toggle dark mode preference")
    public ResponseEntity<ApiResponse<UserResponse>> toggleDarkMode(
            @RequestHeader("Authorization") String auth,
            @RequestParam boolean enabled) {
        return ResponseEntity.ok(ApiResponse.success(
                enabled ? "Dark mode enabled" : "Dark mode disabled",
                userService.toggleDarkMode(uid(auth), enabled)));
    }
}
