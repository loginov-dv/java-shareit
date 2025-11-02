package ru.practicum.shareit.gateway.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostBookingRequest {
    @NotNull(message = "Id бронируемого предмета не может быть пустым")
    private Integer itemId;
    @NotNull(message = "Дата начала бронирования не может быть пустой")
    private String start;
    @NotNull(message = "Дата окончания бронирования не может быть пустой")
    private String end;
}
