package com.travelmaster.user.service;

import com.travelmaster.common.constant.RoleConstants;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private Role travelerRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        travelerRole = Role.builder()
                .id(1L)
                .name(RoleConstants.ROLE_TRAVELER)
                .description("Traveler role")
                .build();

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .enabled(true)
                .deleted(false)
                .roles(Set.of(travelerRole))
                .build();
    }

    @Test
    @DisplayName("Should register new user successfully")
    void shouldRegisterNewUser() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("new@example.com")
                .password("password123")
                .firstName("New")
                .lastName("User")
                .build();

        when(userRepository.existsActiveByEmail(request.getEmail())).thenReturn(false);
        when(roleRepository.findByName(RoleConstants.ROLE_TRAVELER)).thenReturn(Optional.of(travelerRole));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateAccessToken(anyString(), anyString(), anyList())).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refreshToken");
        when(jwtTokenProvider.getAccessTokenValidityMs()).thenReturn(900000L);

        // When
        AuthResponse response = authService.register(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
        assertThat(response.getTokenType()).isEqualTo("Bearer");

        verify(userRepository).existsActiveByEmail(request.getEmail());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(request.getPassword());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@example.com")
                .password("password123")
                .firstName("Existing")
                .lastName("User")
                .build();

        when(userRepository.existsActiveByEmail(request.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("уже существует");

        verify(userRepository).existsActiveByEmail(request.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should login successfully with correct credentials")
    void shouldLoginSuccessfully() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        when(userRepository.findByEmailAndDeletedFalse(request.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(anyString(), anyString(), anyList())).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refreshToken");
        when(jwtTokenProvider.getAccessTokenValidityMs()).thenReturn(900000L);

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();

        verify(userRepository).findByEmailAndDeletedFalse(request.getEmail());
        verify(passwordEncoder).matches(request.getPassword(), testUser.getPassword());
    }

    @Test
    @DisplayName("Should throw exception when login with wrong password")
    void shouldThrowExceptionForWrongPassword() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("wrongPassword")
                .build();

        when(userRepository.findByEmailAndDeletedFalse(request.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.getPassword(), testUser.getPassword())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Неверный email или пароль");

        verify(passwordEncoder).matches(request.getPassword(), testUser.getPassword());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("nonexistent@example.com")
                .password("password123")
                .build();

        when(userRepository.findByEmailAndDeletedFalse(request.getEmail())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ValidationException.class);

        verify(userRepository).findByEmailAndDeletedFalse(request.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when user is disabled")
    void shouldThrowExceptionWhenUserIsDisabled() {
        // Given
        testUser.setEnabled(false);
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        when(userRepository.findByEmailAndDeletedFalse(request.getEmail())).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("заблокирован");

        verify(userRepository).findByEmailAndDeletedFalse(request.getEmail());
    }
}

