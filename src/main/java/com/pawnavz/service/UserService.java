package com.pawnavz.service;

import com.pawnavz.dto.request.UpdateUserRequest;
import com.pawnavz.dto.response.UserResponse;
import com.pawnavz.entity.User;
import com.pawnavz.exception.BadRequestException;
import com.pawnavz.exception.ConflictException;
import com.pawnavz.exception.ResourceNotFoundException;
import com.pawnavz.repository.UserRepository;
import com.pawnavz.util.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    @Cacheable(value = "userProfile", key = "#userId")
    public UserResponse getProfile(String userId) {
        return mapToResponse(findById(userId));
    }

    @Transactional
    @CacheEvict(value = "userProfile", key = "#userId")
    public UserResponse updateProfile(String userId, UpdateUserRequest request) {
        User user = findById(userId);
        boolean changed = false;
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
            changed = true;
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            userRepository.findByPhone(request.getPhone()).ifPresent(existing -> {
                if (!existing.getId().equals(userId)) {
                    throw new ConflictException("Phone number already in use");
                }
            });
            user.setPhone(request.getPhone().trim());
            changed = true;
        }
        if (!changed) throw new BadRequestException("No valid fields provided for update");
        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    @CacheEvict(value = "userProfile", key = "#userId")
    public UserResponse uploadProfileImage(String userId, MultipartFile file) {
        User user = findById(userId);
        if (user.getProfileImageUrl() != null) {
            try { s3Service.delete(user.getProfileImageUrl()); }
            catch (Exception e) { log.warn("Failed to delete old avatar: {}", e.getMessage()); }
        }
        user.setProfileImageUrl(s3Service.upload(file, "profiles"));
        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    @CacheEvict(value = "userProfile", key = "#userId")
    public UserResponse toggleDarkMode(String userId, boolean enabled) {
        User user = findById(userId);
        user.setDarkModeEnabled(enabled);
        return mapToResponse(userRepository.save(user));
    }

    private User findById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    public UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId()).name(user.getName()).email(user.getEmail())
                .phone(user.getPhone()).profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole().name()).darkModeEnabled(user.getDarkModeEnabled())
                .createdAt(user.getCreatedAt()).build();
    }
}

