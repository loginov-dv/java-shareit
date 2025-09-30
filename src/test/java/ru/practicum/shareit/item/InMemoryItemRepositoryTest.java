package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryItemRepositoryTest {
    private final InMemoryItemRepository itemRepository;

    private InMemoryItemRepositoryTest() {
        itemRepository = new InMemoryItemRepository();
    }

    @Test
    void shouldSaveItem() {
        Item item = createItem();

        item = itemRepository.save(item);

        assertNotNull(item.getId());
        assertNotEquals(0, item.getId());
    }

    @Test
    void shouldFindItem() {
        Item item = createItem();

        item = itemRepository.save(item);

        Optional<Item> maybeFoundItem = itemRepository.findById(item.getId());

        if (maybeFoundItem.isEmpty()) {
            fail();
        }

        Item foundItem = maybeFoundItem.get();

        assertEquals(item.getId(), foundItem.getId());
        assertEquals(item.getName(), foundItem.getName());
        assertEquals(item.getDescription(), foundItem.getDescription());
        assertEquals(item.isAvailable(), foundItem.isAvailable());
        assertEquals(item.getOwnerId(), foundItem.getOwnerId());
    }

    @Test
    void shouldNotFindUnknownItem() {
        Optional<Item> maybeFoundItem = itemRepository.findById(9999);

        if (maybeFoundItem.isPresent()) {
            fail();
        }
    }

    @Test
    void shouldFindItemByUserId() {
        Item item1 = createItem();
        Item item2 = createItem();
        item1.setOwnerId(1000);
        item2.setOwnerId(1000);

        item1 = itemRepository.save(item1);
        item2 = itemRepository.save(item2);

        List<Item> items = itemRepository.findByUserId(1000);

        assertEquals(2, items.size());
    }

    @Test
    void shouldUpdateItem() {
        Item item = createItem();

        item = itemRepository.save(item);

        item.setName("new name");
        item.setDescription("new description");
        item.setAvailable(false);

        itemRepository.update(item);

        Optional<Item> maybeFoundItem = itemRepository.findById(item.getId());

        if (maybeFoundItem.isEmpty()) {
            fail();
        }

        Item foundItem = maybeFoundItem.get();

        assertEquals(item.getId(), foundItem.getId());
        assertEquals(item.getName(), foundItem.getName());
        assertEquals(item.getDescription(), foundItem.getDescription());
        assertEquals(item.isAvailable(), foundItem.isAvailable());
        assertEquals(item.getOwnerId(), foundItem.getOwnerId());
    }

    @Test
    void shouldFindItemsBySearchString() {
        Item item1 = createItem();
        Item item2 = createItem();
        item1.setName("rieugeiruge search ewwgw");
        item2.setDescription("search rgeerg eer");

        item1 = itemRepository.save(item1);
        item2 = itemRepository.save(item2);

        List<Item> items = itemRepository.search("search");

        assertEquals(2, items.size());
    }

    private Item createItem() {
        Item item = new Item();

        item.setName(createName());
        item.setDescription(createName(50));
        item.setAvailable(true);
        item.setOwnerId(new Random().nextInt(100));

        return item;
    }

    private String createName() {
        return createName(10);
    }

    private String createName(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        int charsLength = chars.length();
        int counter = 0;
        String result = "";

        while (counter < length) {
            result += chars.charAt((int)Math.round(Math.random() * (charsLength - 1)));
            counter++;
        }

        return result;
    }
}