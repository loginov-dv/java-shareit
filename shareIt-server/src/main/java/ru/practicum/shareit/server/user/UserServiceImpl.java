package ru.practicum.shareit.server.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.shareit.server.exception.EmailConflictException;
import ru.practicum.shareit.server.exception.ExceptionConstants;
import ru.practicum.shareit.server.exception.LogConstants;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.user.dto.PatchUserRequest;
import ru.practicum.shareit.server.user.dto.PostUserRequest;
import ru.practicum.shareit.server.user.dto.UserDto;
import ru.practicum.shareit.server.user.mapper.UserMapper;
import ru.practicum.shareit.server.user.model.User;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto createUser(PostUserRequest request) {
        log.debug("Запрос на создание пользователя: {}", request);

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn(LogConstants.EMAIL_CONFLICT, request.getEmail());
            throw new EmailConflictException(ExceptionConstants.EMAIL_CONFLICT);
        }

        User user = userRepository.save(UserMapper.toUser(request));

        log.debug("Добавлен пользователь: {}", user);

        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findById(int userId) {
        log.debug("Запрос на получение пользователя с id = {}", userId);

        Optional<User> maybeUser = userRepository.findById(userId);

        if (maybeUser.isEmpty()) {
            log.warn(LogConstants.USER_NOT_FOUND_BY_ID, userId);
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, userId));
        }

        log.debug("Пользователь: {}", maybeUser.get());

        return UserMapper.toUserDto(maybeUser.get());
    }

    @Override
    @Transactional
    public UserDto update(int userId, PatchUserRequest request) {
        log.debug("Запрос на обновление пользователя с id = {}", userId);

        Optional<User> maybeUser = userRepository.findById(userId);

        if (maybeUser.isEmpty()) {
            log.warn(LogConstants.USER_NOT_FOUND_BY_ID, userId);
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, userId));
        }

        if (request.hasEmail()) {
            Optional<User> found = userRepository.findByEmail(request.getEmail());

            if (found.isPresent() && found.get().getId() != userId) {
                log.warn(LogConstants.EMAIL_CONFLICT, request.getEmail());
                throw new EmailConflictException(ExceptionConstants.EMAIL_CONFLICT);
            }
        }

        User user = maybeUser.get();
        log.debug("Исходное состояние пользователя: {}", user);

        UserMapper.updateUserFields(user, request);

        user = userRepository.save(user);
        log.debug("Изменён пользователь: {}", user);

        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public void deleteById(int userId) {
        log.debug("Запрос на удаление пользователя с id = {}", userId);
        userRepository.deleteById(userId);
        log.debug("Удалён пользователь с id = {}", userId);
    }
}
