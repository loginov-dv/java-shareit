package ru.practicum.shareit.server.request;

import ru.practicum.shareit.server.request.dto.ItemRequestDto;
import ru.practicum.shareit.server.request.dto.ItemRequestShortDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestShortDto createRequest(int userId, ItemRequestShortDto dto);

    List<ItemRequestDto> findByUserId(int userId);

    List<ItemRequestShortDto> findAll(int userId);

    ItemRequestDto findById(int requestId);
}
