package ru.practicum.shareit.item.dto;

import lombok.Data;

import java.util.List;

@Data
public class ItemOwnerDto {
    private Integer id;
    private Integer ownerId;
    private String name;
    private String description;
    private Boolean available;
    private Integer requestId;
    private String lastBooking;
    private String nextBooking;
    private List<CommentDto> comments;
}
