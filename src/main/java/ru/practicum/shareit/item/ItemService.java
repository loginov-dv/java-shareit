package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.dto.PatchItemRequest;

import java.util.List;

public interface ItemService {
    ItemShortDto createItem(int userId, ItemShortDto itemDto);

    ItemDto findById(int userId, int itemId);

    List<ItemDto> findByUserId(int userId);

    List<ItemShortDto> search(String text);

    ItemShortDto update(int userId, int itemId, PatchItemRequest request);

    CommentDto createComment(int userId, int itemId, CommentDto commentDto);
}
