package ru.practicum.shareit.gateway.booking.dto;

import lombok.Data;

@Data
public class BookingShortDto {
    private Integer id;
    private String start;
    private String end;
    private String status;
}
