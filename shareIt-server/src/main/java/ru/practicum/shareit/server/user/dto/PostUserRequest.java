package ru.practicum.shareit.server.user.dto;

import lombok.Data;

@Data
public class PostUserRequest {
    private String name;
    private String email;
}
