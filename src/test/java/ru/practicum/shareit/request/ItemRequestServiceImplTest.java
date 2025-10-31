package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utils.RandomUtils;

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
        User requestor = createUser();
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
        User requestor = createUser();
        ItemRequest request1 = createRequest(requestor);
        ItemRequest request2 = createRequest(requestor);
        // предметы только для первого запроса
        List<Item> items = List.of(createItem(request1), createItem(request1));

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
        List<ItemRequest> requests = List.of(createRequest(createUser()), createRequest(createUser()));

        when(itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(anyInt()))
                .thenReturn(requests);

        List<ItemRequestShortDto> result = requestService.findAll(999);

        assertEquals(requests.size(), result.size());
    }

    @Test
    void shouldFindRequestById() {
        ItemRequest request = createRequest(createUser());
        List<Item> items = List.of(createItem(request), createItem(request));

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

    private Item createItem(ItemRequest request) {
        Item item = new Item();

        item.setId(random.nextInt(100));
        item.setName(RandomUtils.createName());
        item.setDescription(RandomUtils.createName(50));
        item.setAvailable(true);
        item.setOwner(createUser());
        item.setRequestId(request.getId());

        return item;
    }

    private ItemRequest createRequest(User requestor) {
        ItemRequest request = new ItemRequest();

        request.setId(random.nextInt(100));
        request.setCreated(LocalDateTime.now().minusMinutes(random.nextLong(5, 100)));
        request.setDescription(RandomUtils.createName(50));
        request.setRequestor(requestor);

        return request;
    }

    private User createUser(int id) {
        User owner = new User();
        owner.setId(id);
        owner.setName(RandomUtils.createName());
        owner.setEmail(owner.getName() + "@mail.ru");
        return owner;
    }

    private User createUser() {
        return createUser(random.nextInt(100));
    }
}