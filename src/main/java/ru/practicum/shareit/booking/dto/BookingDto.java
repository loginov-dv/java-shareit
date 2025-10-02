package ru.practicum.shareit.booking.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingDto {
    private Integer id;
    private Integer itemId;
    private Integer bookerId;
    private LocalDateTime start;
    private LocalDateTime end;
    private String status;
}
