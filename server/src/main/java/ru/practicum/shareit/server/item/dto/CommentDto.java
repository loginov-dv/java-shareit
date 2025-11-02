package ru.practicum.shareit.server.item.dto;

import lombok.Data;

@Data
public class CommentDto {
    private Integer id;
    private String text;
    private Integer itemId;
    private String authorName;
    private String created;
}
