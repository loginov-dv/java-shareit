package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.dto.PatchItemRequest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.format.DateTimeFormatter;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemMapper {
    public static ItemShortDto toItemShortDto(Item item) {
        ItemShortDto dto = new ItemShortDto();

        dto.setId(item.getId());
        dto.setOwnerId(item.getOwner().getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.isAvailable());
        dto.setRequestId(item.getRequestId());

        return dto;
    }

    public static ItemShortDto toItemShortDto(Item item, List<Comment> comments) {
        ItemShortDto dto = toItemShortDto(item);

        if (comments != null && !comments.isEmpty()) {
            dto.setComments(comments.stream().map(CommentMapper::toCommentDto).toList());
        }

        return dto;
    }

    public static Item toNewItem(User user, ItemShortDto dto) {
        Item item = new Item();

        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setOwner(user);
        item.setAvailable(dto.getAvailable());

        return item;
    }

    public static Item toItem(User user, ItemShortDto dto) {
        Item item = toNewItem(user, dto);

        if (dto.getId() != null) {
            item.setId(dto.getId());
        }

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

    public static ItemDto toItemDto(Item item, Booking lastBooking, Booking nextBooking,
                                    List<Comment> comments) {
        ItemDto dto = new ItemDto();

        dto.setId(item.getId());
        dto.setOwnerId(item.getOwner().getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.isAvailable());
        //dto.setRequestId(item.getRequestId());

        if (lastBooking != null) {
            dto.setLastBooking(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(lastBooking.getStart()));
        }

        if (nextBooking != null) {
            dto.setNextBooking(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(nextBooking.getEnd()));
        }

        if (comments != null && !comments.isEmpty()) {
            dto.setComments(comments.stream().map(CommentMapper::toCommentDto).toList());
        }

        return dto;
    }
}
