package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.PatchUserRequest;
import ru.practicum.shareit.user.dto.PostUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

public interface UserService {
    UserDto add(PostUserRequest request);

    UserDto getById(int userId);

    UserDto update(int userId, PatchUserRequest request);

    void delete(int userId);
}
