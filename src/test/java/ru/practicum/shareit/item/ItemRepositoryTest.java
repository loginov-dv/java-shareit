package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utils.RandomUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
// используем настройки из application-test.properties
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"/schema.sql", "/clear.sql"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRepositoryTest {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository requestRepository;

    @Test
    void shouldSaveItem() {
        Item item = createItem();
        item = itemRepository.save(item);

        assertNotNull(item.getId());
    }

    @Test
    void shouldFindItemById() {
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
        itemRepository.save(item1);
        itemRepository.save(item2);

        List<Item> items = itemRepository.search("search");

        assertEquals(2, items.size());
    }

    @Test
    void shouldDeleteItem() {
        Item item = createItem();
        item = itemRepository.save(item);

        itemRepository.deleteById(item.getId());

        Optional<Item> maybeFoundItem = itemRepository.findById(item.getId());

        if (maybeFoundItem.isPresent()) {
            fail();
        }
    }

    @Test
    void shouldFindItemByRequestId() {
        ItemRequest request = createRequest();
        Item itemForRequest = createItem();

        itemForRequest.setRequestId(request.getId());
        itemRepository.save(itemForRequest);

        List<Item> items = itemRepository.findByRequestId(request.getId());

        assertEquals(1, items.size());
    }

    @Test
    void shouldFindItemByRequestIdIn() {
        ItemRequest request1 = createRequest();
        ItemRequest request2 = createRequest();
        Item item1 = createItem();
        Item item2 = createItem();

        item1.setRequestId(request1.getId());
        item2.setRequestId(request2.getId());
        itemRepository.save(item1);
        itemRepository.save(item2);

        List<Item> items = itemRepository.findByRequestIdIn(List.of(request1.getId(), request2.getId()));

        assertEquals(2, items.size());
        assertTrue(items.contains(item1));
        assertTrue(items.contains(item2));
    }

    // для предмета сначала создаётся владелец
    private Item createItem() {
        User user = createUser();
        Item item = new Item();

        item.setName(RandomUtils.createName());
        item.setDescription(RandomUtils.createName(50));
        item.setAvailable(true);
        item.setOwner(user);

        return item;
    }

    // с сохранением в таблицу users
    private User createUser() {
        User user = new User();
        String name = RandomUtils.createName();

        user.setName(name);
        user.setEmail(name + "@mail.ru");

        user = userRepository.save(user);

        return user;
    }

    private ItemRequest createRequest() {
        User requestor = createUser();
        ItemRequest request = new ItemRequest();

        request.setRequestor(requestor);
        request.setDescription(RandomUtils.createName(20));
        request = requestRepository.save(request);

        return request;
    }
}