package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
// используем настройки из application-test.properties
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"/schema.sql"})
class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveItem() {
        Item item = createItem();

        item = itemRepository.save(item);

        assertNotNull(item.getId());
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
        assertEquals(item.getOwner().getId(), foundItem.getOwner().getId());
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
        Item item = createItem();

        item = itemRepository.save(item);

        List<Item> items = itemRepository.findByOwnerId(item.getOwner().getId());

        assertEquals(1, items.size());
    }

    @Test
    void shouldUpdateItem() {
        Item item = createItem();

        item = itemRepository.save(item);

        item.setName("new name");
        item.setDescription("new description");
        item.setAvailable(false);

        itemRepository.save(item);

        Optional<Item> maybeFoundItem = itemRepository.findById(item.getId());

        if (maybeFoundItem.isEmpty()) {
            fail();
        }

        Item foundItem = maybeFoundItem.get();

        assertEquals(item.getId(), foundItem.getId());
        assertEquals(item.getName(), foundItem.getName());
        assertEquals(item.getDescription(), foundItem.getDescription());
        assertEquals(item.isAvailable(), foundItem.isAvailable());
        assertEquals(item.getOwner().getId(), foundItem.getOwner().getId());
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
        User user = createUser();
        user = userRepository.save(user);

        Item item = new Item();

        item.setName(createName());
        item.setDescription(createName(50));
        item.setAvailable(true);
        item.setOwner(user);

        return item;
    }

    private User createUser() {
        User user = new User();
        String name = createName();

        user.setName(name);
        user.setEmail(name + "@mail.ru");

        return user;
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