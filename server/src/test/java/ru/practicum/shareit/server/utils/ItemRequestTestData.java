package ru.practicum.shareit.server.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.server.request.dto.ItemRequestDto;
import ru.practicum.shareit.server.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemRequestTestData {
    // без id для сохранения в базу
    public static ItemRequest createNewRequest(User requestor) {
        ItemRequest request = new ItemRequest();

        request.setRequestor(requestor);
        request.setDescription(RandomUtils.createName(20));
        request.setCreated(LocalDateTime.now());

        return request;
    }

    // с рандомным id
    public static ItemRequest createRequest(User requestor) {
        ItemRequest request = new ItemRequest();

        request.setId(new Random().nextInt(100));
        request.setRequestor(requestor);
        request.setDescription(RandomUtils.createName(20));
        request.setCreated(LocalDateTime.now());

        return request;
    }

    // с рандомным id
    public static ItemRequestShortDto createNewItemRequest() {
        ItemRequestShortDto dto = new ItemRequestShortDto();

        dto.setRequestorId(new Random().nextInt(100));
        dto.setDescription(RandomUtils.createName(50));

        return dto;
    }

    // с рандомным id и id реквестора
    public static ItemRequestDto createItemRequestDto() {
        ItemRequestDto dto = new ItemRequestDto();
        Random random = new Random();

        dto.setId(random.nextInt(100));
        dto.setCreated(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()));
        dto.setRequestorId(random.nextInt(100));
        dto.setDescription(RandomUtils.createName(50));

        return dto;
    }

    // с рандомным id и id реквестора
    public static ItemRequestShortDto createItemRequestShortDto() {
        ItemRequestShortDto dto = new ItemRequestShortDto();
        Random random = new Random();

        dto.setId(random.nextInt(100));
        dto.setCreated(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()));
        dto.setRequestorId(random.nextInt(100));
        dto.setDescription(RandomUtils.createName(50));

        return dto;
    }
}
