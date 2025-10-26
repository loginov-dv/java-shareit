package ru.practicum.shareit.item.dto;

import lombok.Data;

@Data
public class ItemShortDto {
    private Integer id;
    private String name;
    private Integer ownerId;
}
