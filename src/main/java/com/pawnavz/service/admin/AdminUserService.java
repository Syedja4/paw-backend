package com.pawnavz.service.admin;

import com.pawnavz.dto.response.UserResponse;
import com.pawnavz.entity.User;
import com.pawnavz.exception.BadRequestException;
import com.pawnavz.exception.ResourceNotFoundException;
import com.pawnavz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    public Page<UserResponse> getAllUsers(String search, String role, Boolean blocked, Pageable pageable) {
        System.out.println("ADMIN API HIT");
        try {
            User.Role parsedRole = parseRole(role);
            Page<User> users = userRepository.findWithFilters(search, parsedRole, blocked, pageable);
            return users != null ? users.map(this::mapToResponse) : Page.empty(pageable);
        } catch (Exception e) {
            e.printStackTrace();
            return Page.empty(pageable);
        }
    }

    public UserResponse getUserById(String id) {
        System.out.println("ADMIN API HIT");
        try {
            return mapToResponse(findById(id));
        } catch (Exception e) {
            e.printStackTrace();
            return emptyUserResponse();
        }
    }

    @Transactional
    public UserResponse blockUser(String id) {
        System.out.println("ADMIN API HIT");
        try {
            User user = findById(id);
            if (Boolean.TRUE.equals(user.getBlocked())) throw new BadRequestException("User is already blocked");
            user.setBlocked(true);
            return mapToResponse(userRepository.save(user));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public UserResponse unblockUser(String id) {
        System.out.println("ADMIN API HIT");
        try {
            User user = findById(id);
            if (!Boolean.TRUE.equals(user.getBlocked())) throw new BadRequestException("User is not blocked");
            user.setBlocked(false);
            return mapToResponse(userRepository.save(user));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private User findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private User.Role parseRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        try {
            return User.Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid role: " + role);
        }
    }

    private UserResponse mapToResponse(User user) {
        LocalDateTime fallbackTime = Optional.ofNullable(user.getCreatedAt()).orElse(LocalDateTime.now());
        return UserResponse.builder()
                .id(Optional.ofNullable(user.getId()).orElse(""))
                .name(Optional.ofNullable(user.getName()).orElse(""))
                .email(Optional.ofNullable(user.getEmail()).orElse(""))
                .phone(Optional.ofNullable(user.getPhone()).orElse(""))
                .profileImageUrl(Optional.ofNullable(user.getProfileImageUrl()).orElse(""))
                .role(user.getRole() != null ? user.getRole().name() : "")
                .darkModeEnabled(Optional.ofNullable(user.getDarkModeEnabled()).orElse(Boolean.FALSE))
                .blocked(Optional.ofNullable(user.getBlocked()).orElse(Boolean.FALSE))
                .createdAt(fallbackTime)
                .lastLoginAt(fallbackTime)
                .build();
    }

    private UserResponse emptyUserResponse() {
        LocalDateTime now = LocalDateTime.now();
        return UserResponse.builder()
                .id("")
                .name("")
                .email("")
                .phone("")
                .profileImageUrl("")
                .role("")
                .darkModeEnabled(Boolean.FALSE)
                .blocked(Boolean.FALSE)
                .createdAt(now)
                .lastLoginAt(now)
                .build();
    }
}
