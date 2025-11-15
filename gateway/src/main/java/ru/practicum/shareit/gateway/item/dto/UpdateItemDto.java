package ru.practicum.shareit.gateway.item.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateItemDto {
    @Pattern(regexp = "(?!\\s*$).+", message = "Новое имя не может быть пустым или состоять только из пробелов")
    private String name;
    private String description;
    private Boolean available;
}
