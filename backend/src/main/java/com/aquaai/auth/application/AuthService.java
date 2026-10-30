package com.aquaai.auth.application;

import com.aquaai.auth.api.dto.LoginRequest;
import com.aquaai.auth.api.dto.LoginResponse;
import com.aquaai.auth.api.dto.RegisterRequest;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.security.JwtTokenProvider;
import com.aquaai.user.domain.User;
import com.aquaai.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("Attempting authentication for username/phone: {}", request.username());

        User user = userRepository.findByPhoneNumber(request.username())
                .or(() -> userRepository.findByEmail(request.username()))
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        if (!user.isActive()) {
            throw new BusinessRuleException("User account is inactive. Please contact support.");
        }

        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getPhoneNumber(), user.getRole());
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        log.info("Registering new user with phone: {}", request.phoneNumber());

        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new BusinessRuleException("User with this phone number already exists");
        }

        if (request.email() != null && !request.email().isBlank() && userRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("User with this email already exists");
        }

        User user = User.builder()
                .phoneNumber(request.phoneNumber())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(request.role())
                .district(request.district() != null ? request.district() : "Yangiariq")
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        String accessToken = tokenProvider.generateAccessToken(savedUser.getId(), savedUser.getPhoneNumber(), savedUser.getRole());
        String refreshToken = tokenProvider.generateRefreshToken(savedUser.getId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole())
                .build();
    }
}
