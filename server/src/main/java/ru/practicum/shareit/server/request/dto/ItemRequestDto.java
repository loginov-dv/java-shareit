package ru.practicum.shareit.server.request.dto;

import lombok.Data;

import ru.practicum.shareit.server.item.dto.ItemShortDto;

import java.util.List;

@Data
public class ItemRequestDto {
    private Integer id;
    private String description;
    private Integer requestorId;
    private String created;
    private List<ItemShortDto> items;
}
