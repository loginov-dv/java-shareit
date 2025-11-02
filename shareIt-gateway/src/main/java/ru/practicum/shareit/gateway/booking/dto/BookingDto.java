package ru.practicum.shareit.gateway.booking.dto;

import lombok.Data;
import ru.practicum.shareit.gateway.item.dto.ItemDto;
import ru.practicum.shareit.gateway.user.dto.UserDto;

@Data
public class BookingDto {
    private Integer id;
    private ItemDto item;
    private UserDto booker;
    private String start;
    private String end;
    private String status;
}
