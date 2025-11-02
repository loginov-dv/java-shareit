package ru.practicum.shareit.server.item.dto;

import lombok.Data;

import java.util.List;

@Data
public class ItemDto {
    private Integer id;
    private Integer ownerId;
    private String name;
    private String description;
    private Boolean available;
    private Integer requestId;
    private List<CommentDto> comments;
}
