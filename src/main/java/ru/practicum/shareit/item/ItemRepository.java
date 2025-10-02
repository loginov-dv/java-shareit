package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item save(Item item);

    Optional<Item> findById(int itemId);

    List<Item> findByUserId(int userId);

    List<Item> search(String text);

    void update(Item item);
}
