package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PatchUserRequest {
    @Pattern(regexp = ".+", message = "Имя не может быть пустым")
    private String name;
    @Pattern(regexp = "^\\S+$", message = "Электронная почта не может быть пустой или содержать пробелы")
    @Email(message = "Электронная почта не соответствует формату")
    private String email;

    public boolean hasName() {
        return name != null;
    }

    public boolean hasEmail() {
        return email != null;
    }
}
