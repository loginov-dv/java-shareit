package ru.practicum.shareit.gateway.item.dto;

import lombok.Data;

@Data
public class ItemShortDto {
    private Integer id;
    private String name;
    private Integer ownerId;
}
