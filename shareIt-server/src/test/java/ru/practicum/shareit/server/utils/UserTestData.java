package ru.practicum.shareit.server.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.server.user.dto.PatchUserRequest;
import ru.practicum.shareit.server.user.dto.PostUserRequest;
import ru.practicum.shareit.server.user.dto.UserDto;
import ru.practicum.shareit.server.user.model.User;

import java.util.Random;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserTestData {
    // без id для сохранения в БД
    public static User createNewUser() {
        User user = new User();
        String name = RandomUtils.createName();

        user.setName(name);
        user.setEmail(name + "@mail.ru");

        return user;
    }

    // с рандомным id
    public static User createUser() {
        User user = new User();
        String name = RandomUtils.createName();

        user.setId(new Random().nextInt(100));
        user.setName(name);
        user.setEmail(name + "@mail.ru");

        return user;
    }

    public static PostUserRequest createPostUserRequest() {
        PostUserRequest request = new PostUserRequest();

        request.setName(RandomUtils.createName());
        request.setEmail(request.getName() + "@mail.ru");

        return request;
    }

    // с рандомным id
    public static UserDto createUserDto(PostUserRequest request) {
        UserDto dto = new UserDto();

        dto.setId(new Random().nextInt(100));
        dto.setName(request.getName());
        dto.setEmail(request.getEmail());

        return dto;
    }

    public static UserDto createUserDto(int id) {
        UserDto dto = new UserDto();

        dto.setId(id);
        dto.setName(RandomUtils.createName());
        dto.setEmail(dto.getName() + "@mail.ru");

        return dto;
    }

    // с рандомным id
    public static UserDto createUserDto() {
        UserDto dto = new UserDto();

        dto.setId(new Random().nextInt(100));
        dto.setName(RandomUtils.createName());
        dto.setEmail(dto.getName() + "@mail.ru");

        return dto;
    }

    public static PatchUserRequest createPatchUserRequest() {
        PatchUserRequest request = new PatchUserRequest();

        request.setName(RandomUtils.createName());
        request.setEmail(request.getName() + "@mail.ru");

        return request;
    }
}
