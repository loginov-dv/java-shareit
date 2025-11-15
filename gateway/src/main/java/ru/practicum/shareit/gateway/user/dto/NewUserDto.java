package ru.practicum.shareit.gateway.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class NewUserDto {
    @NotBlank(message = "Имя не может быть пустым")
    private String name;
    @NotBlank(message = "Электронная почта не может быть пустой")
    @Pattern(regexp = "^\\S+$", message = "Электронная почта не может содержать пробелы")
    @Email(message = "Электронная почта не соответствует формату")
    private String email;
}
