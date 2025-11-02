package ru.practicum.shareit.server.request.dto;

import lombok.Data;

@Data
public class ItemRequestShortDto {
    private Integer id;
    private String description;
    private Integer requestorId;
    private String created;
}
