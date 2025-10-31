package com.travelmaster.user.service;

import com.travelmaster.common.exception.EntityNotFoundException;
import com.travelmaster.user.dto.UpdateUserRequest;
import com.travelmaster.user.dto.UserResponse;
import com.travelmaster.user.entity.User;
import com.travelmaster.user.mapper.UserMapper;
import com.travelmaster.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserResponse testUserResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+79991234567")
                .enabled(true)
                .deleted(false)
                .build();

        testUserResponse = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+79991234567")
                .build();
    }

    @Test
    @DisplayName("Should get user by id successfully")
    void shouldGetUserById() {
        // Given
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse response = userService.getUserById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");

        verify(userRepository).findByIdAndDeletedFalse(1L);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(userRepository).findByIdAndDeletedFalse(999L);
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUser() {
        // Given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("Updated")
                .lastName("Name")
                .phoneNumber("+79991111111")
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Updated")
                .lastName("Name")
                .phoneNumber("+79991111111")
                .build();

        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toResponse(updatedUser)).thenReturn(UserResponse.builder()
                .id(1L)
                .firstName("Updated")
                .lastName("Name")
                .phoneNumber("+79991111111")
                .build());

        // When
        UserResponse response = userService.updateUser(1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getFirstName()).isEqualTo("Updated");
        assertThat(response.getLastName()).isEqualTo("Name");
        assertThat(response.getPhoneNumber()).isEqualTo("+79991111111");

        verify(userRepository).findByIdAndDeletedFalse(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should delete (anonymize) user successfully")
    void shouldDeleteUser() {
        // Given
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).findByIdAndDeletedFalse(1L);
        verify(userRepository).save(argThat(user ->
                user.getDeleted() &&
                !user.getEnabled() &&
                user.getFirstName().equals("DELETED") &&
                user.getLastName().equals("USER")
        ));
    }
}

