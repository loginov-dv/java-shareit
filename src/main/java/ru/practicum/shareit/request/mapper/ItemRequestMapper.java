package ru.practicum.shareit.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.format.DateTimeFormatter;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemRequestMapper {

    public static ItemRequest toNewItemRequest(User requestor, ItemRequestShortDto dto) {
        ItemRequest request = new ItemRequest();

        request.setDescription(dto.getDescription());
        request.setRequestor(requestor);

        return request;
    }

    public static ItemRequestShortDto toItemRequestShortDto(ItemRequest request) {
        ItemRequestShortDto dto = new ItemRequestShortDto();

        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(request.getCreated()));
        dto.setRequestorId(request.getRequestor().getId());

        return dto;
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest request, List<Item> items) {
        ItemRequestDto dto = new ItemRequestDto();

        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(request.getCreated()));
        dto.setRequestorId(request.getRequestor().getId());
        dto.setItems(items.stream().map(ItemMapper::toItemShortDto).toList());

        return dto;
    }
}
