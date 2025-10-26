package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDetailedDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.PatchItemRequest;

import java.util.List;

public interface ItemService {
    ItemDto createItem(int userId, ItemDto itemDto);

    ItemDetailedDto findById(int userId, int itemId);

    List<ItemDetailedDto> findByUserId(int userId);

    List<ItemDto> search(String text);

    ItemDto update(int userId, int itemId, PatchItemRequest request);

    CommentDto createComment(int userId, int itemId, CommentDto commentDto);
}
