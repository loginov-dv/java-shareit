package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.PatchItemRequest;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto add(int userId, ItemDto itemDto);

    ItemDto findById(int itemId);

    List<ItemDto> findByUser(int userId);

    List<ItemDto> search(String text);

    ItemDto update(int userId, int itemId, PatchItemRequest request);
}
