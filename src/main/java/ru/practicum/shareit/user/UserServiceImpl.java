package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ExceptionConstants;
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
    public UserDto add(PostUserRequest request) {
        User user = UserMapper.toUser(request);
        user = userRepository.save(user);

        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getById(int userId) {
        Optional<User> maybeUser = userRepository.findById(userId);

        if (maybeUser.isEmpty()) {
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, userId));
        }

        return UserMapper.toUserDto(maybeUser.get());
    }

    @Override
    public UserDto update(int userId, PatchUserRequest request) {
        Optional<User> maybeUser = userRepository.findById(userId);

        if (maybeUser.isEmpty()) {
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, userId));
        }

        User user = maybeUser.get();
        UserMapper.updateUserFields(user, request);

        userRepository.update(user);

        return UserMapper.toUserDto(user);
    }

    @Override
    public void delete(int userId) {
        userRepository.delete(userId);
    }
}
