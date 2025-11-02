package ru.practicum.shareit.server.booking.dto;

import lombok.Data;

@Data
public class PostBookingRequest {
    private Integer itemId;
    private String start;
    private String end;
}
