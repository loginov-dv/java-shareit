package ru.practicum.shareit.server.item.dto;

import lombok.Data;
import ru.practicum.shareit.server.booking.dto.BookingShortDto;

import java.util.List;

@Data
public class ItemDetailedDto {
    private Integer id;
    private Integer ownerId;
    private String name;
    private String description;
    private Boolean available;
    private Integer requestId;
    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;
    private List<CommentDto> comments;
}
