package ru.practicum.shareit.item.dto;

import lombok.Data;

@Data
public class ItemDto {
    private Integer id;
    private Integer ownerId;
    private String name;
    private String description;
    private boolean availableForRent;
    private Integer requestId;
}
