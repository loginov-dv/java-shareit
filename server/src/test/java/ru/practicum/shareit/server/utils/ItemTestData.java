package ru.practicum.shareit.server.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.server.item.dto.CommentDto;
import ru.practicum.shareit.server.item.dto.ItemDetailedDto;
import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.item.dto.PatchItemRequest;
import ru.practicum.shareit.server.item.model.Comment;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.user.model.User;

import java.util.Random;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemTestData {
    // без id для сохранения в БД
    public static Item createNewItem(User owner) {
        Item item = new Item();

        item.setName(RandomUtils.createName());
        item.setDescription(RandomUtils.createName(50));
        item.setAvailable(true);
        item.setOwner(owner);

        return item;
    }

    // с рандомным id
    public static Item createItem(User owner) {
        Item item = new Item();

        item.setId(new Random().nextInt(100));
        item.setName(RandomUtils.createName());
        item.setDescription(RandomUtils.createName(50));
        item.setAvailable(true);
        item.setOwner(owner);

        return item;
    }

    // с рандомным id
    public static Item createItem(User owner, ItemRequest request) {
        Item item = new Item();

        item.setId(new Random().nextInt(100));
        item.setName(RandomUtils.createName());
        item.setDescription(RandomUtils.createName(50));
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequestId(request.getId());

        return item;
    }

    public static ItemDto createNewItemDto() {
        ItemDto request = new ItemDto();

        request.setName(RandomUtils.createName());
        request.setDescription(RandomUtils.createName(50));
        request.setAvailable(true);

        return request;
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

    public static PatchItemRequest createPatchItemRequest() {
        PatchItemRequest request = new PatchItemRequest();

        request.setName(RandomUtils.createName());
        request.setDescription(RandomUtils.createName(50));
        request.setAvailable(false);

        return request;
    }

    // без id для сохранения в БД
    public static Comment createNewComment(Item item, User author) {
        Comment comment = new Comment();

        comment.setItem(item);
        comment.setAuthor(author);
        comment.setText(RandomUtils.createName(100));

        return comment;
    }

    // с рандомным id
    public static Comment createComment(Item item, User author) {
        Comment comment = new Comment();

        comment.setId(new Random().nextInt(100));
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setText(RandomUtils.createName(100));

        return comment;
    }

    public static CommentDto createNewCommentDto() {
        CommentDto commentDto = new CommentDto();

        commentDto.setText(RandomUtils.createName(50));

        return commentDto;
    }
}
