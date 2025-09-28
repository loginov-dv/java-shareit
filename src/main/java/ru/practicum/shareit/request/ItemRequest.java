package ru.practicum.shareit.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of = "id")
public class ItemRequest {
    private Integer id;
    private String description;
    private Integer requestorId;
    private LocalDateTime created;
}
