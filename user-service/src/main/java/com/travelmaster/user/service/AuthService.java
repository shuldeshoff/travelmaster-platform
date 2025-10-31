package com.travelmaster.user.service;

import com.travelmaster.common.constant.RoleConstants;
import com.travelmaster.common.exception.BusinessException;
import com.travelmaster.common.exception.ValidationException;
import com.travelmaster.user.dto.AuthResponse;
import com.travelmaster.user.dto.LoginRequest;
import com.travelmaster.user.dto.RegisterRequest;
import com.travelmaster.user.entity.Role;
import com.travelmaster.user.entity.User;
import com.travelmaster.user.mapper.UserMapper;
import com.travelmaster.user.repository.RoleRepository;
import com.travelmaster.user.repository.UserRepository;
import com.travelmaster.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Регистрация нового пользователя: {}", request.getEmail());

        // Проверка существования пользователя
        if (userRepository.existsActiveByEmail(request.getEmail())) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }

        // Получение роли TRAVELER
        Role travelerRole = roleRepository.findByName(RoleConstants.ROLE_TRAVELER)
                .orElseThrow(() -> new BusinessException("Роль TRAVELER не найдена"));

        // Создание пользователя
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .enabled(true)
                .deleted(false)
                .build();

        user.addRole(travelerRole);

        User savedUser = userRepository.save(user);
        log.info("Пользователь успешно зарегистрирован: id={}, email={}", 
                savedUser.getId(), savedUser.getEmail());

        return createAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Попытка входа: {}", request.getEmail());

        User user = userRepository.findByEmailAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new ValidationException("Неверный email или пароль"));

        if (!user.getEnabled()) {
            throw new ValidationException("Аккаунт заблокирован");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Неверный пароль для пользователя: {}", request.getEmail());
            throw new ValidationException("Неверный email или пароль");
        }

        log.info("Успешный вход: id={}, email={}", user.getId(), user.getEmail());
        return createAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("INVALID_TOKEN", "Невалидный refresh token");
        }

        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Пользователь не найден"));

        if (user.getDeleted() || !user.getEnabled()) {
            throw new BusinessException("ACCOUNT_DISABLED", "Аккаунт заблокирован");
        }

        return createAuthResponse(user);
    }

    private AuthResponse createAuthResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId().toString(),
                user.getEmail(),
                roles
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId().toString()
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenValidityMs() / 1000)
                .user(userMapper.toResponse(user))
                .build();
    }
}

