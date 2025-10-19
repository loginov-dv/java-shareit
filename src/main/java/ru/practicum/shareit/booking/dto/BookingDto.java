package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserDto;

@Data
public class BookingDto {
    private Integer id;
    private ItemShortDto item;
    private UserDto booker;
    private String start;
    private String end;
    private String status;
}
