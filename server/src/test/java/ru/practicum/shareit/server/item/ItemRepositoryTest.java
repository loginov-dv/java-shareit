package ru.practicum.shareit.server.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.request.ItemRequestRepository;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.user.UserRepository;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.utils.ItemRequestTestData;
import ru.practicum.shareit.server.utils.ItemTestData;
import ru.practicum.shareit.server.utils.UserTestData;

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
        User owner = userRepository.save(UserTestData.createNewUser());
        Item item = itemRepository.save(ItemTestData.createNewItem(owner));

        assertNotNull(item.getId());
    }

    @Test
    void shouldFindItemById() {
        User owner = userRepository.save(UserTestData.createNewUser());
        Item item = itemRepository.save(ItemTestData.createNewItem(owner));

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
        User owner = userRepository.save(UserTestData.createNewUser());
        Item item = itemRepository.save(ItemTestData.createNewItem(owner));

        List<Item> items = itemRepository.findByOwnerId(item.getOwner().getId());

        assertEquals(1, items.size());
    }

    @Test
    void shouldUpdateItem() {
        User owner = userRepository.save(UserTestData.createNewUser());

        Item item = itemRepository.save(ItemTestData.createNewItem(owner));
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
        User owner = userRepository.save(UserTestData.createNewUser());

        Item item1 = ItemTestData.createNewItem(owner);
        Item item2 = ItemTestData.createNewItem(owner);
        item1.setName("rieugeiruge search ewwgw");
        item2.setDescription("search rgeerg eer");

        itemRepository.save(item1);
        itemRepository.save(item2);

        List<Item> items = itemRepository.search("search");

        assertEquals(2, items.size());
    }

    @Test
    void shouldDeleteItem() {
        User owner = userRepository.save(UserTestData.createNewUser());
        Item item = itemRepository.save(ItemTestData.createNewItem(owner));

        itemRepository.deleteById(item.getId());

        Optional<Item> maybeFoundItem = itemRepository.findById(item.getId());

        if (maybeFoundItem.isPresent()) {
            fail();
        }
    }

    @Test
    void shouldFindItemByRequestId() {
        User owner = userRepository.save(UserTestData.createNewUser());

        User requestor = userRepository.save(UserTestData.createNewUser());

        ItemRequest request = requestRepository.save(ItemRequestTestData.createNewRequest(requestor));

        Item itemForRequest = ItemTestData.createNewItem(owner);
        itemForRequest.setRequestId(request.getId());
        itemRepository.save(itemForRequest);

        List<Item> items = itemRepository.findByRequestId(request.getId());

        assertEquals(1, items.size());
    }

    @Test
    void shouldFindItemByRequestIdIn() {
        User owner = userRepository.save(UserTestData.createNewUser());

        Item item1 = ItemTestData.createNewItem(owner);
        Item item2 = ItemTestData.createNewItem(owner);

        User requestor = userRepository.save(UserTestData.createNewUser());

        ItemRequest request1 = requestRepository.save(ItemRequestTestData.createNewRequest(requestor));
        ItemRequest request2 = requestRepository.save(ItemRequestTestData.createNewRequest(requestor));
        item1.setRequestId(request1.getId());
        item2.setRequestId(request2.getId());

        itemRepository.save(item1);
        itemRepository.save(item2);

        List<Item> items = itemRepository.findByRequestIdIn(List.of(request1.getId(), request2.getId()));

        assertEquals(2, items.size());
        assertTrue(items.contains(item1));
        assertTrue(items.contains(item2));
    }
}