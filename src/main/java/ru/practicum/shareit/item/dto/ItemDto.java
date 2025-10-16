package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ItemDto {
    private Integer id;
    private Integer ownerId;
    @NotBlank(message = "Наименование не может быть пустым")
    private String name;
    @NotBlank(message = "Описание не может быть пустым")
    private String description;
    @NotNull(message = "Статус должен быть задан")
    private Boolean available;
    private Integer requestId;
    private String lastBooking = null;
    private String nextBooking = null;
    private List<CommentDto> comments;
}
