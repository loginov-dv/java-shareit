package ru.practicum.shareit.gateway.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.gateway.user.dto.PatchUserRequest;
import ru.practicum.shareit.gateway.user.dto.PostUserRequest;
import ru.practicum.shareit.gateway.user.dto.UserDto;

import java.util.Random;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserTestData {
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
