package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.PatchItemRequest;
import ru.practicum.shareit.item.model.Item;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        ItemDto itemDto = new ItemDto();

        itemDto.setId(item.getId());
        itemDto.setOwnerId(item.getOwnerId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.isAvailable());
        itemDto.setRequestId(item.getRequestId());

        return itemDto;
    }

    public static Item toItem(int userId, ItemDto itemDto) {
        Item item = new Item();

        if (itemDto.getId() != null) {
            item.setId(itemDto.getId());
        }

        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setOwnerId(userId);
        item.setAvailable(itemDto.getAvailable());

        return item;
    }

    public static void updateItemFields(Item item, PatchItemRequest request) {
        if (request.hasName()) {
            item.setName(request.getName());
        }

        if (request.hasDescription()) {
            item.setDescription(request.getDescription());
        }

        if (request.hasAvailable()) {
            item.setAvailable(request.getAvailable());
        }
    }
}
