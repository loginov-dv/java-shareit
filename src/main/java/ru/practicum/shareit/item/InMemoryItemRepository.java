package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@Slf4j
@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Integer, Item> items = new HashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public Item save(Item item) {
        log.debug("Запрос на создание предмета");
        item.setId(counter.incrementAndGet());
        items.put(item.getId(), item);

        return item;
    }

    @Override
    public Optional<Item> findById(int itemId) {
        log.debug("Запрос на получение предмета с id = {}", itemId);
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public List<Item> findByUserId(int userId) {
        log.debug("Запрос на получение предметов пользователя с id = {}", userId);
        return items.values().stream()
                .filter(item -> item.getOwnerId().equals(userId))
                .toList();
    }

    @Override
    public List<Item> search(String text) {
        log.debug("Запрос на поиск предметов с text = {}", text);
        Pattern pattern = Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE);

        return items.values().stream()
                .filter(item -> item.isAvailable() && (pattern.matcher(item.getName()).find()
                        || pattern.matcher(item.getDescription()).find()))
                .toList();
    }

    @Override
    public void update(Item item) {
        log.debug("Запрос на изменение предмета с id = {}", item.getId());
        items.put(item.getId(), item);
    }
}
