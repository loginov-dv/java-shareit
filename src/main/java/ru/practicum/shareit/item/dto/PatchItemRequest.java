package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PatchItemRequest {
    @Pattern(regexp = ".+", message = "Имя не может быть пустым")
    private String name;
    private String description;
    private Boolean available;

    public boolean hasName() {
        return name != null;
    }

    public boolean hasDescription() {
        return description != null;
    }

    public boolean hasAvailable() {
        return available != null;
    }
}
