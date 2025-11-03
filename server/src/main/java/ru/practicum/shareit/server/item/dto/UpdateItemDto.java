package ru.practicum.shareit.server.item.dto;

import lombok.Data;

@Data
public class UpdateItemDto {
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
