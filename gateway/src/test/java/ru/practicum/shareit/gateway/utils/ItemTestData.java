package ru.practicum.shareit.gateway.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.gateway.item.dto.CommentDto;
import ru.practicum.shareit.gateway.item.dto.ItemDetailedDto;
import ru.practicum.shareit.gateway.item.dto.ItemDto;
import ru.practicum.shareit.gateway.item.dto.UpdateItemDto;
import ru.practicum.shareit.gateway.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemTestData {
    public static ItemDto createNewItemDto() {
        ItemDto request = new ItemDto();

        request.setName(RandomUtils.createName());
        request.setDescription(RandomUtils.createName(50));
        request.setAvailable(true);

        return request;
    }

    public static ItemDto createItemDto(ItemDto newItemDto) {
        ItemDto dto = new ItemDto();
        Random random = new Random();

        dto.setId(random.nextInt(100));
        dto.setOwnerId(random.nextInt(100));
        dto.setName(newItemDto.getName());
        dto.setDescription(newItemDto.getDescription());
        dto.setAvailable(newItemDto.getAvailable());

        return dto;
    }

    // с рандомными id и id владельца
    public static ItemDto createItemDto() {
        ItemDto item = ItemTestData.createNewItemDto();
        Random random = new Random();

        item.setId(random.nextInt(100));
        item.setOwnerId(random.nextInt(100));

        return item;
    }

    // с рандомным id владельца
    public static ItemDto createItemDto(int id) {
        ItemDto item = ItemTestData.createNewItemDto();
        Random random = new Random();

        item.setId(id);
        item.setOwnerId(random.nextInt(100));

        return item;
    }

    // с рандомными id и id владельца
    public static ItemDetailedDto createItemDetailedDto() {
        ItemDetailedDto dto = new ItemDetailedDto();
        Random random = new Random();

        dto.setId(random.nextInt(100));
        dto.setName(RandomUtils.createName());
        dto.setDescription(RandomUtils.createName(50));
        dto.setAvailable(true);
        dto.setOwnerId(random.nextInt(100));

        return dto;
    }

    public static UpdateItemDto createUpdateItemDto() {
        UpdateItemDto request = new UpdateItemDto();

        request.setName(RandomUtils.createName());
        request.setDescription(RandomUtils.createName(50));
        request.setAvailable(false);

        return request;
    }

    public static CommentDto createNewCommentDto() {
        CommentDto commentDto = new CommentDto();

        commentDto.setText(RandomUtils.createName(50));

        return commentDto;
    }

    public static CommentDto createCommentDto(ItemDto item, UserDto author, CommentDto newCommentDto) {
        CommentDto commentDto = new CommentDto();
        Random random = new Random();

        commentDto.setId(random.nextInt(100));
        commentDto.setText(newCommentDto.getText());
        commentDto.setItemId(item.getId());
        commentDto.setAuthorName(author.getName());
        commentDto.setCreated(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()));

        return commentDto;
    }
}
