package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentDto {
    private Integer id;
    /*@NotBlank(message = "Текст комментария не должен быть пустым")*/
    private String text;
    private Integer itemId;
    private String authorName;
    private String created;
}
