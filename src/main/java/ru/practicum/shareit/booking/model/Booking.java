package ru.practicum.shareit.booking.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of = "id")
public class Booking {
    private Integer id;
    private Integer itemId;
    private Integer bookerId;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;
}
