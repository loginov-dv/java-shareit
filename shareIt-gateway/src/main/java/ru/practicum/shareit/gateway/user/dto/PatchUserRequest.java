package ru.practicum.shareit.gateway.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PatchUserRequest {
    @Pattern(regexp = "^(?!\\s*$).+", message = "Новое имя не может быть пустым или состоять только из пробелов")
    private String name;
    @Pattern(regexp = "^\\S+$", message = "Новая электронная почта не может быть пустой или содержать пробелы")
    @Email(message = "Новая электронная почта не соответствует формату")
    private String email;
}
