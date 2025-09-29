package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.PatchUserRequest;
import ru.practicum.shareit.user.dto.PostUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

public interface UserService {
    UserDto createUser(PostUserRequest request);

    UserDto findById(int userId);

    UserDto update(int userId, PatchUserRequest request);

    void deleteById(int userId);
}
