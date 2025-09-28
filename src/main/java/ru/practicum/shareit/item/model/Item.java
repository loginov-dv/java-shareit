package ru.practicum.shareit.item.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Data
@EqualsAndHashCode(of = "id")
public class Item {
    private Integer id;
    private User owner;
    private String name;
    private String description;
    private boolean available;
    private ItemRequest request;
}
