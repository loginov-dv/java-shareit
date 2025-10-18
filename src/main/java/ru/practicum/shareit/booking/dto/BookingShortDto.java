package ru.practicum.shareit.booking.dto;

import lombok.Data;

@Data
public class BookingShortDto {
    private Integer id;
    private String start;
    private String end;
    private String status;
}
