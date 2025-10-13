package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EmailConflictException;
import ru.practicum.shareit.exception.ExceptionConstants;
import ru.practicum.shareit.exception.LogConstants;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.PatchUserRequest;
import ru.practicum.shareit.user.dto.PostUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto createUser(PostUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn(LogConstants.EMAIL_CONFLICT, request.getEmail());
            throw new EmailConflictException(ExceptionConstants.EMAIL_CONFLICT);
        }

        User user = UserMapper.toUser(request);
        user = userRepository.save(user);

        log.debug("Добавлен пользователь: {}", user);

        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto findById(int userId) {
        Optional<User> maybeUser = userRepository.findById(userId);

        if (maybeUser.isEmpty()) {
            log.warn(LogConstants.USER_NOT_FOUND_BY_ID, userId);
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, userId));
        }

        log.debug("Найден пользователь по id = {}: {}", userId, maybeUser.get());

        return UserMapper.toUserDto(maybeUser.get());
    }

    @Override
    public UserDto update(int userId, PatchUserRequest request) {
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
        userRepository.save(user);

        log.debug("Изменён пользователь: {}", user);

        return UserMapper.toUserDto(user);
    }

    @Override
    public void deleteById(int userId) {
        userRepository.deleteById(userId);
        log.debug("Удалён пользователь с id = {}", userId);
    }
}
