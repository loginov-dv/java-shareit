package ru.practicum.shareit.server.user;

import ru.practicum.shareit.server.user.dto.UpdateUserDto;
import ru.practicum.shareit.server.user.dto.NewUserDto;
import ru.practicum.shareit.server.user.dto.UserDto;

public interface UserService {
    UserDto createUser(NewUserDto request);

    UserDto findById(int userId);

    UserDto update(int userId, UpdateUserDto request);

    void deleteById(int userId);
}
