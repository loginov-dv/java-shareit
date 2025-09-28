package ru.practicum.shareit.request.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of = "id")
public class ItemRequest {
    private Integer id;
    private String description;
    private User requestor;
    private LocalDateTime created;
}
