package ru.practicum.shareit.item.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")
public class Item {
    private Integer id;
    private Integer ownerId;
    private String name;
    private String description;
    private boolean available;
    private Integer requestId;
}
