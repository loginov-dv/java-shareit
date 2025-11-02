package ru.practicum.shareit.server.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import ru.practicum.shareit.server.booking.mapper.BookingMapper;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.item.dto.ItemDetailedDto;
import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.item.dto.ItemShortDto;
import ru.practicum.shareit.server.item.dto.PatchItemRequest;
import ru.practicum.shareit.server.item.model.Comment;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.user.model.User;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        ItemDto dto = new ItemDto();

        dto.setId(item.getId());
        dto.setOwnerId(item.getOwner().getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.isAvailable());
        dto.setRequestId(item.getRequestId());

        return dto;
    }

    public static ItemDto toItemDto(Item item, List<Comment> comments) {
        ItemDto dto = toItemDto(item);

        if (comments != null && !comments.isEmpty()) {
            dto.setComments(comments.stream().map(CommentMapper::toCommentDto).toList());
        }

        return dto;
    }

    public static Item toNewItem(User user, ItemDto dto) {
        Item item = new Item();

        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setOwner(user);
        item.setAvailable(dto.getAvailable());
        item.setRequestId(dto.getRequestId());

        return item;
    }

    public static Item toItem(User user, ItemDto dto) {
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

    public static ItemDetailedDto toItemDetailedDto(Item item, Booking lastBooking, Booking nextBooking,
                                                    List<Comment> comments) {
        ItemDetailedDto dto = new ItemDetailedDto();

        dto.setId(item.getId());
        dto.setOwnerId(item.getOwner().getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.isAvailable());
        dto.setRequestId(item.getRequestId());

        if (lastBooking != null) {
            dto.setLastBooking(BookingMapper.toBookingShortDto(lastBooking));
        }

        if (nextBooking != null) {
            dto.setNextBooking(BookingMapper.toBookingShortDto(nextBooking));
        }

        if (comments != null && !comments.isEmpty()) {
            dto.setComments(comments.stream().map(CommentMapper::toCommentDto).toList());
        }

        return dto;
    }

    public static ItemShortDto toItemShortDto(Item item) {
        ItemShortDto dto = new ItemShortDto();

        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setOwnerId(item.getOwner().getId());

        return dto;
    }
}
