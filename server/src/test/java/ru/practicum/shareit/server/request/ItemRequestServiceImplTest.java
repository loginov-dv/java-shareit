package ru.practicum.shareit.server.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.item.ItemRepository;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.request.dto.ItemRequestDto;
import ru.practicum.shareit.server.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.user.UserRepository;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.utils.ItemRequestTestData;
import ru.practicum.shareit.server.utils.ItemTestData;
import ru.practicum.shareit.server.utils.RandomUtils;
import ru.practicum.shareit.server.utils.UserTestData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
class ItemRequestServiceImplTest {
    @Autowired
    private ItemRequestService requestService;
    @MockBean
    private ItemRequestRepository itemRequestRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private ItemRepository itemRepository;

    private final Random random = new Random();

    @Test
    void shouldCreateRequest() {
        ItemRequestShortDto request = new ItemRequestShortDto();
        request.setDescription(RandomUtils.createName(50));

        User requestor = UserTestData.createUser();

        ItemRequest savedRequest = new ItemRequest();
        savedRequest.setCreated(LocalDateTime.now());
        savedRequest.setRequestor(requestor);
        savedRequest.setId(random.nextInt(100));
        savedRequest.setDescription(request.getDescription());

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(requestor));
        when(itemRequestRepository.save(any(ItemRequest.class)))
                .thenReturn(savedRequest);

        ItemRequestShortDto result = requestService.createRequest(requestor.getId(), request);

        assertEquals(savedRequest.getId(), result.getId());
        assertEquals(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(savedRequest.getCreated()), result.getCreated());
        assertEquals(savedRequest.getDescription(), result.getDescription());
        assertEquals(savedRequest.getRequestor().getId(), result.getRequestorId());
    }

    @Test
    void shouldNotCreateRequestIfRequestorNotFound() {
        ItemRequestShortDto request = new ItemRequestShortDto();
        request.setDescription(RandomUtils.createName(50));

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.createRequest(999, request));
    }

    @Test
    void shouldFindAllRequestsByRequestorId() {
        User requestor = UserTestData.createUser();

        ItemRequest request1 = ItemRequestTestData.createRequest(requestor);
        ItemRequest request2 = ItemRequestTestData.createRequest(requestor);
        // предметы только для первого запроса
        List<Item> items = List.of(
                ItemTestData.createItem(UserTestData.createUser(), request1),
                ItemTestData.createItem(UserTestData.createUser(), request1)
        );

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(requestor));
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(anyInt()))
                .thenReturn(List.of(request1, request2));
        when(itemRepository.findByRequestIdIn(anyList()))
                .thenReturn(items);

        List<ItemRequestDto> result = requestService.findByUserId(requestor.getId());

        // у первого dto есть items
        Optional<ItemRequestDto> maybeDto1 = result.stream()
                .filter(dto -> dto.getId().equals(request1.getId()))
                .findFirst();

        if (maybeDto1.isEmpty()) {
            fail();
        }

        ItemRequestDto dto1 = maybeDto1.get();

        assertEquals(2, dto1.getItems().size());

        // у второго dto нет items
        Optional<ItemRequestDto> maybeDto2 = result.stream()
                .filter(dto -> dto.getId().equals(request2.getId()))
                .findFirst();

        if (maybeDto2.isEmpty()) {
            fail();
        }

        ItemRequestDto dto2 = maybeDto2.get();

        assertEquals(0, dto2.getItems().size());
    }

    @Test
    void shouldNotFindAllRequestsByRequestorIdIfRequestorNotFound() {
        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.findByUserId(999));
    }

    @Test
    void shouldFindAllRequests() {
        List<ItemRequest> requests = List.of(
                ItemRequestTestData.createRequest(UserTestData.createUser()),
                ItemRequestTestData.createRequest(UserTestData.createUser())
        );

        when(itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(anyInt()))
                .thenReturn(requests);

        List<ItemRequestShortDto> result = requestService.findAll(999);

        assertEquals(requests.size(), result.size());
    }

    @Test
    void shouldFindRequestById() {
        ItemRequest request = ItemRequestTestData.createRequest(UserTestData.createUser());

        List<Item> items = List.of(
                ItemTestData.createItem(UserTestData.createUser(), request),
                ItemTestData.createItem(UserTestData.createUser(), request)
        );

        when(itemRequestRepository.findById(anyInt()))
                .thenReturn(Optional.of(request));
        when(itemRepository.findByRequestId(anyInt()))
                .thenReturn(items);

        ItemRequestDto result = requestService.findById(request.getId());

        assertEquals(request.getId(), result.getId());
        assertEquals(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(request.getCreated()), result.getCreated());
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(request.getRequestor().getId(), result.getRequestorId());
        assertEquals(items.size(), result.getItems().size());
    }

    /*private ItemRequest createRequest(User requestor) {
        ItemRequest request = new ItemRequest();

        request.setId(random.nextInt(100));
        request.setCreated(LocalDateTime.now().minusMinutes(random.nextLong(5, 100)));
        request.setDescription(RandomUtils.createName(50));
        request.setRequestor(requestor);

        return request;
    }*/
}