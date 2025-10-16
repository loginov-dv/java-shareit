package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.PatchItemRequest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemMapper {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd hh:mm:ss")
            .withZone(ZoneOffset.UTC);

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

        if (!comments.isEmpty()) {
            dto.setComments(comments.stream().map(CommentMapper::toCommentDto).toList());
        }



        return dto;
    }

    /*public static Item toItem(int userId, ItemDto itemDto) {
        Item item = new Item();

        if (itemDto.getId() != null) {
            item.setId(itemDto.getId());
        }

        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setOwnerId(userId);
        item.setAvailable(itemDto.getAvailable());

        return item;
    }*/

    public static Item toNewItem(User user, ItemDto dto) {
        Item item = new Item();

        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setOwner(user);
        item.setAvailable(dto.getAvailable());

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

    public static ItemOwnerDto toItemOwnerDto(Item item, Booking lastBooking, Booking nextBooking,
                                              List<Comment> comments) {
        ItemOwnerDto dto = new ItemOwnerDto();

        dto.setId(item.getId());
        dto.setOwnerId(item.getOwner().getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.isAvailable());
        //dto.setRequestId(item.getRequestId());

        if (lastBooking != null) {
            dto.setLastBooking(formatter.format(lastBooking.getStart()));
        }

        if (nextBooking != null) {
            dto.setNextBooking(formatter.format(nextBooking.getEnd()));
        }

        // TODO: null и empty не записываются
        if (comments != null && !comments.isEmpty()) {
            dto.setComments(comments.stream().map(CommentMapper::toCommentDto).toList());
        }

        return dto;
    }
}
