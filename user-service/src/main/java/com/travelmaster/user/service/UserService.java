package com.travelmaster.user.service;

import com.travelmaster.common.exception.EntityNotFoundException;
import com.travelmaster.user.dto.UpdateUserRequest;
import com.travelmaster.user.dto.UserResponse;
import com.travelmaster.user.entity.User;
import com.travelmaster.user.mapper.UserMapper;
import com.travelmaster.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAllActive(pageable)
                .map(userMapper::toResponse);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Обновление профиля пользователя: id={}", id);

        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        User updatedUser = userRepository.save(user);
        log.info("Профиль пользователя обновлён: id={}", id);

        return userMapper.toResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Удаление (анонимизация) пользователя: id={}", id);

        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));

        // Анонимизация вместо удаления (для сохранения целостности данных)
        user.setEmail("deleted-" + user.getId() + "@anonymized.com");
        user.setFirstName("DELETED");
        user.setLastName("USER");
        user.setPhoneNumber(null);
        user.setPassportNumber(null);
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setEnabled(false);

        userRepository.save(user);
        log.info("Пользователь анонимизирован: id={}", id);
    }
}

