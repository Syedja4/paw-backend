package com.pawnavz.service;

import com.pawnavz.dto.request.LoginRequest;
import com.pawnavz.dto.request.SignupRequest;
import com.pawnavz.dto.response.AuthResponse;
import com.pawnavz.dto.response.UserResponse;
import com.pawnavz.entity.Cart;
import com.pawnavz.entity.User;
import com.pawnavz.entity.Wishlist;
import com.pawnavz.exception.ConflictException;
import com.pawnavz.exception.UnauthorizedException;
import com.pawnavz.repository.CartRepository;
import com.pawnavz.repository.UserRepository;
import com.pawnavz.repository.WishlistRepository;
import com.pawnavz.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final WishlistRepository wishlistRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(User.Role.USER)
                .build();
        user = userRepository.save(user);
        cartRepository.save(Cart.builder().user(user).build());
        wishlistRepository.save(Wishlist.builder().user(user).build());
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        AuthResponse response = buildAuthResponse(user);
        user.setRefreshToken(response.getRefreshToken());
        userRepository.save(user);
        return response;
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtUtil.extractUsername(refreshToken);
        String tokenType = jwtUtil.extractTokenType(refreshToken);
        if (!"REFRESH".equals(tokenType)) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new UnauthorizedException("Refresh token revoked");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        if (!jwtUtil.isTokenValid(refreshToken, userDetails)) {
            throw new UnauthorizedException("Refresh token expired");
        }
        String newAccessToken = jwtUtil.generateAccessToken(userDetails, user.getId(), normalizeRole(user.getRole().name()));
        String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(900)
                .user(mapToUserResponse(user))
                .build();
    }

    @Transactional
    public void logout(String userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setRefreshToken(null);
            userRepository.save(user);
        });
    }

    private AuthResponse buildAuthResponse(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtUtil.generateAccessToken(userDetails, user.getId(), normalizeRole(user.getRole().name()));
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900)
                .user(mapToUserResponse(user))
                .build();
    }

    public static UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole().name())
                .darkModeEnabled(user.getDarkModeEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private static String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_USER";
        }
        String normalized = role.trim().toUpperCase();
        return normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized;
    }
}
