package ru.practicum.shareit.server.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.item.ItemService;
import ru.practicum.shareit.server.item.ItemServiceImpl;
import ru.practicum.shareit.server.request.dto.ItemRequestDto;
import ru.practicum.shareit.server.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.server.user.UserService;
import ru.practicum.shareit.server.user.UserServiceImpl;
import ru.practicum.shareit.server.user.dto.UserDto;
import ru.practicum.shareit.server.utils.ItemRequestTestData;
import ru.practicum.shareit.server.utils.ItemTestData;
import ru.practicum.shareit.server.utils.UserTestData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@Transactional
@DataJpaTest
@Import(value = {ItemServiceImpl.class, UserServiceImpl.class, ItemRequestServiceImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"/schema.sql", "/clear.sql"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceImplIntegrationTest {
    private final ItemRequestService requestService;
    private final ItemService itemService;
    private final UserService userService;

    @Test
    void shouldCreateItemRequest() {
        UserDto requestor = userService.createUser(UserTestData.createNewUserDto());
        ItemRequestShortDto newRequest = ItemRequestTestData.createNewItemRequest();
        ItemRequestShortDto savedRequest = requestService.createRequest(requestor.getId(), newRequest);

        assertNotNull(savedRequest.getId());
        assertNotNull(savedRequest.getCreated());
        assertEquals(requestor.getId(), savedRequest.getRequestorId());
        assertEquals(newRequest.getDescription(), savedRequest.getDescription());
    }

    @Test
    void shouldNotCreateItemRequestIfRequestorNotFount() {
        assertThrows(NotFoundException.class,
                () -> requestService.createRequest(999, ItemRequestTestData.createNewItemRequest()));
    }

    @Test
    void shouldFindItemRequestById() {
        UserDto requestor = userService.createUser(UserTestData.createNewUserDto());
        ItemRequestShortDto savedRequest = requestService.createRequest(requestor.getId(),
                ItemRequestTestData.createNewItemRequest());

        ItemRequestDto foundRequest = requestService.findById(savedRequest.getId());

        assertEquals(savedRequest.getId(), foundRequest.getId());
        assertEquals(savedRequest.getDescription(), foundRequest.getDescription());
        assertEquals(savedRequest.getRequestorId(), foundRequest.getRequestorId());
        assertEquals(savedRequest.getCreated(), foundRequest.getCreated());
        assertEquals(0, foundRequest.getItems().size());
    }

    @Test
    void shouldNotFindUnknownItemRequest() {
        assertThrows(NotFoundException.class, () -> requestService.findById(999));
    }

    @Test
    void shouldFindAllItemRequestsByUserId() {
        // добавим двух пользователей
        UserDto requestor1 = userService.createUser(UserTestData.createNewUserDto());
        UserDto requestor2 = userService.createUser(UserTestData.createNewUserDto());

        // добавим каждому два запроса
        ItemRequestShortDto request11 = requestService.createRequest(requestor1.getId(),
                ItemRequestTestData.createNewItemRequest());
        ItemRequestShortDto request12 = requestService.createRequest(requestor1.getId(),
                ItemRequestTestData.createNewItemRequest());
        ItemRequestShortDto request21 = requestService.createRequest(requestor2.getId(),
                ItemRequestTestData.createNewItemRequest());
        ItemRequestShortDto request22 = requestService.createRequest(requestor2.getId(),
                ItemRequestTestData.createNewItemRequest());
        List<ItemRequestShortDto> requests = List.of(request11, request12, request21, request22);

        // для каждого запроса добавим один предмет, а также один предмет без запроса
        for (ItemRequestShortDto request : requests) {
            UserDto owner = userService.createUser(UserTestData.createNewUserDto());
            itemService.createItem(owner.getId(), ItemTestData.createNewItemDto(request.getId()));
            itemService.createItem(owner.getId(), ItemTestData.createNewItemDto());
        }

        // получим все запросы для requestor1
        List<ItemRequestDto> userRequests = requestService.findByUserId(requestor1.getId());

        assertEquals(2, userRequests.size());
        // запросы должны возвращаться отсортированными от более новых к более старым
        assertEquals(request12.getId(), userRequests.getFirst().getId());
        for (ItemRequestDto request : userRequests) {
            assertEquals(1, request.getItems().size());
        }
    }
}