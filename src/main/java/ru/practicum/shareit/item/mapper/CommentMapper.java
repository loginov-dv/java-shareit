package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentMapper {
    public static CommentDto toCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();

        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setItemId(comment.getItem().getId());
        dto.setAuthorName(comment.getAuthor().getName());
        dto.setCreated(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(comment.getCreated()));

        return dto;
    }

    public static Comment toComment(User user, Item item, CommentDto dto) {
        Comment comment = new Comment();

        comment.setText(dto.getText());
        comment.setAuthor(user);
        comment.setItem(item);

        return comment;
    }
}
