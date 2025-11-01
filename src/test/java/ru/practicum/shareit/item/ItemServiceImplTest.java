package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NoAccessException;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailedDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.PatchItemRequest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utils.BookingTestData;
import ru.practicum.shareit.utils.ItemTestData;
import ru.practicum.shareit.utils.UserTestData;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
class ItemServiceImplTest {
    @Autowired
    private ItemService itemService;
    @MockBean
    private ItemRepository itemRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private BookingRepository bookingRepository;
    @MockBean
    private CommentRepository commentRepository;

    private final Random random = new Random();

    @Test
    void shouldCreateItem() {
        ItemDto request = ItemTestData.createNewItemDto();

        User owner = UserTestData.createUser();

        Item savedItem = new Item();
        savedItem.setId(random.nextInt(100));
        savedItem.setName(request.getName());
        savedItem.setDescription(request.getDescription());
        savedItem.setAvailable(request.getAvailable());
        savedItem.setOwner(owner);

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class)))
                .thenReturn(savedItem);

        ItemDto result = itemService.createItem(owner.getId(), request);

        assertEquals(savedItem.getId(), result.getId());
        assertEquals(savedItem.getName(), result.getName());
        assertEquals(savedItem.getDescription(), result.getDescription());
        assertEquals(savedItem.isAvailable(), result.getAvailable());
        assertEquals(savedItem.getOwner().getId(), result.getOwnerId());
    }

    @Test
    void shouldNotCreateItemOfUnknownUser() {
        ItemDto request = ItemTestData.createNewItemDto();

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createItem(999, request));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldFindItemById(boolean requestFromOwner) {
        Item item = ItemTestData.createItem(UserTestData.createUser());

        // бронирования
        Booking pastBooking = BookingTestData.createBooking(item, UserTestData.createUser(), true);
        Booking futureBooking = BookingTestData.createBooking(item, UserTestData.createUser(), false);
        List<Booking> bookings = Stream.of(pastBooking, futureBooking)
                .sorted(Comparator.comparing(Booking::getStart))
                .collect(Collectors.toCollection(LinkedList::new));

        // комментарии
        Comment comment = ItemTestData.createComment(item, pastBooking.getBooker());
        List<Comment> comments = List.of(comment);

        // мокируем вызовы методов репозитория
        when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findByItemIdOrderByStart(anyInt()))
                .thenReturn(bookings);
        when(commentRepository.findByItemId(anyInt()))
                .thenReturn(comments);

        ItemDetailedDto result;
        if (requestFromOwner) {
            result = itemService.findById(item.getOwner().getId(), item.getId());
        } else {
            result = itemService.findById(999, item.getId());
        }

        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.isAvailable(), result.getAvailable());
        assertEquals(item.getOwner().getId(), result.getOwnerId());
        assertNotNull(result.getComments());

        // бронирования показываются только владельцу
        if (requestFromOwner) {
            assertNotNull(result.getLastBooking());
            assertNotNull(result.getNextBooking());
        } else {
            assertNull(result.getLastBooking());
            assertNull(result.getNextBooking());
        }
    }

    @Test
    void shouldNotFindUnknownItemById() {
        when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.findById(1, 1));
    }

    @Test
    void shouldFindItemsByUserId() {
        User owner = UserTestData.createUser();
        Item item1 = ItemTestData.createItem(owner);
        Item item2 = ItemTestData.createItem(owner);
        List<Item> items = List.of(item1, item2);

        // завершённое бронирование и отзыв только для первого предмета
        User booker = UserTestData.createUser();
        List<Booking> bookings = List.of(BookingTestData.createBooking(item1, booker, true));
        List<Comment> comments = List.of(ItemTestData.createComment(item1, booker));

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerId(anyInt()))
                .thenReturn(items);
        when(commentRepository.findByItemIdIn(anyList()))
                .thenReturn(comments);
        when(bookingRepository.findByItemIdInOrderByStart(anyList()))
                .thenReturn(bookings);

        List<ItemDetailedDto> result = itemService.findByUserId(owner.getId());

        assertEquals(items.size(), result.size());

        // у первого dto есть lastBooking и comments
        Optional<ItemDetailedDto> maybeDto1 = result.stream()
                .filter(dto -> dto.getId().equals(item1.getId()))
                .findFirst();

        if (maybeDto1.isEmpty()) {
            fail();
        }

        ItemDetailedDto dto1 = maybeDto1.get();

        assertNotNull(dto1.getLastBooking());
        assertNull(dto1.getNextBooking());
        assertEquals(comments.size(), dto1.getComments().size());

        // у второго dto нет ни bookings, ни comments
        Optional<ItemDetailedDto> maybeDto2 = result.stream()
                .filter(dto -> dto.getId().equals(item2.getId()))
                .findFirst();

        if (maybeDto2.isEmpty()) {
            fail();
        }

        ItemDetailedDto dto2 = maybeDto2.get();

        assertNull(dto2.getLastBooking());
        assertNull(dto2.getNextBooking());
        assertNull(dto2.getComments());
    }

    @Test
    void shouldNotFindItemsOfUnknownUser() {
        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.findByUserId(1));
    }

    @Test
    void shouldFindItemsBySearchString() {
        List<Item> items = List.of(ItemTestData.createItem(UserTestData.createUser()),
                ItemTestData.createItem(UserTestData.createUser()));

        when(itemRepository.search(anyString()))
                .thenReturn(items);

        List<ItemDto> result = itemService.search("text");

        assertEquals(items.size(), result.size());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldNotFindItemsIfSearchStringWasNullOrEmpty(String text) {
        List<ItemDto> items = itemService.search(text);

        assertEquals(0, items.size());
    }

    @Test
    void shouldUpdateItem() {
        PatchItemRequest request = ItemTestData.createPatchItemRequest();

        User owner = UserTestData.createUser();
        Item savedItem = ItemTestData.createItem(owner);

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.of(savedItem));

        ItemDto updatedItem = itemService.update(owner.getId(), savedItem.getId(), request);

        assertEquals(savedItem.getId(), updatedItem.getId());
        assertEquals(request.getName(), updatedItem.getName());
        assertEquals(request.getDescription(), updatedItem.getDescription());
        assertEquals(request.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void shouldNotUpdateItemIfOwnerNotFound() {
        PatchItemRequest request = ItemTestData.createPatchItemRequest();

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.update(1, 1, request));
    }

    @Test
    void shouldNotUpdateItemIfItemNotFound() {
        PatchItemRequest request = ItemTestData.createPatchItemRequest();

        User owner = UserTestData.createUser();

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.update(1, 1, request));
    }

    @Test
    void shouldNotUpdateItemIfNoAccess() {
        PatchItemRequest request = ItemTestData.createPatchItemRequest();

        User owner = UserTestData.createUser();
        Item savedItem = ItemTestData.createItem(owner);

        User someSuspiciousPerson = UserTestData.createUser();

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(someSuspiciousPerson));
        when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.of(savedItem));

        assertThrows(NoAccessException.class, () -> itemService.update(999, savedItem.getId(), request));
    }

    @Test
    void shouldCreateComment() {
        CommentDto commentDto = ItemTestData.createNewCommentDto();

        User author = UserTestData.createUser();
        Item item = ItemTestData.createItem(UserTestData.createUser());

        Booking pastBooking = BookingTestData.createBooking(item, author, true);

        Comment savedComment = ItemTestData.createComment(item, author);
        savedComment.setText(commentDto.getText());

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(author));
        when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(anyInt(), any()))
                .thenReturn(List.of(pastBooking));
        when(commentRepository.save(any(Comment.class)))
                .thenReturn(savedComment);

        CommentDto result = itemService.createComment(author.getId(), item.getId(), commentDto);

        assertEquals(savedComment.getText(), result.getText());
        assertEquals(savedComment.getId(), result.getId());
        assertEquals(savedComment.getItem().getId(), result.getItemId());
        assertEquals(savedComment.getAuthor().getName(), result.getAuthorName());
    }

    @Test
    void shouldNotCreateCommentIfUserHasNoPastBookings() {
        CommentDto commentDto = ItemTestData.createNewCommentDto();

        User author = UserTestData.createUser();
        Item item = ItemTestData.createItem(UserTestData.createUser());

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(author));
        when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(anyInt(), any()))
                .thenReturn(Collections.emptyList());

        assertThrows(NotAvailableException.class,
                () -> itemService.createComment(author.getId(), item.getId(), commentDto));
    }

    @Test
    void shouldNotCreateCommentIfAuthorNotFound() {
        CommentDto commentDto = ItemTestData.createNewCommentDto();

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createComment(1,1,commentDto));
    }

    @Test
    void shouldNotCreateCommentIfItemNotFound() {
        CommentDto commentDto = ItemTestData.createNewCommentDto();

        User author = UserTestData.createUser();

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(author));
        when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createComment(1,1,commentDto));
    }
}