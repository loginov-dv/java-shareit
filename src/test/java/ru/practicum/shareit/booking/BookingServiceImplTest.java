package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.PostBookingRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utils.RandomUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
class BookingServiceImplTest {
    @Autowired
    private BookingService bookingService;
    @MockBean
    private BookingRepository bookingRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private ItemRepository itemRepository;

    private final Random random = new Random();

    @Test
    void shouldCreateBooking() {
        Item item = createItem();
        User booker = createUser();
        PostBookingRequest request = new PostBookingRequest();
        request.setItemId(item.getId());
        request.setStart(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusHours(1)));
        request.setEnd(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusHours(2)));

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.of(item));

        Booking booking = createBooking(item, booker, request);

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        BookingDto result = bookingService.createBooking(booker.getId(), request);

        assertEquals(booking.getId(), result.getId());
        assertEquals(request.getStart(), result.getStart());
        assertEquals(request.getEnd(), result.getEnd());
        assertEquals(item.getId(), result.getItem().getId());
        assertEquals(booker.getId(), result.getBooker().getId());
        assertEquals(booking.getStatus().name(), result.getStatus());
    }

    @Test
    void shouldNotCreateBookingForUnknownUser() {
        Item item = createItem();
        PostBookingRequest request = new PostBookingRequest();
        request.setItemId(item.getId());
        request.setStart(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusHours(1)));
        request.setEnd(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusHours(2)));

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(999, request));
    }

    @Test
    void shouldNotCreateBookingOfUnknownItem() {
        User booker = createUser();
        PostBookingRequest request = new PostBookingRequest();
        request.setItemId(999);
        request.setStart(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusHours(1)));
        request.setEnd(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusHours(2)));

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(booker.getId(), request));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidDates")
    void shouldNotCreateBookingIfDatesAreInvalid(String start, String end) {
        Item item = createItem();
        User booker = createUser();
        PostBookingRequest request = new PostBookingRequest();
        request.setItemId(item.getId());
        request.setStart(start);
        request.setEnd(end);

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.of(item));

        assertThrows(BookingDateException.class, () -> bookingService.createBooking(booker.getId(), request));
    }

    private static Stream<Arguments> provideInvalidDates() {
        return Stream.of(
                // start после end
                Arguments.of(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusHours(2)),
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusHours(1))),
                // start и end равны
                Arguments.of(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusHours(1)),
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusHours(1))),
                // start в прошлом
                Arguments.of(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().minusHours(1)),
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusHours(1))),
                // end в прошлом
                Arguments.of(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusHours(1)),
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().minusHours(1)))
        );
    }

    @Test
    void shouldNotCreateBookingIfItemIsUnavailable() {
        Item item = createItem();
        item.setAvailable(false);
        User booker = createUser();
        PostBookingRequest request = new PostBookingRequest();
        request.setItemId(item.getId());
        request.setStart(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusHours(1)));
        request.setEnd(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusHours(2)));

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.of(item));

        assertThrows(NotAvailableException.class, () -> bookingService.createBooking(booker.getId(), request));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldChangeBookingStatusByOwner(boolean approved) {
        User owner = createUser();
        Item item = createItem(owner);
        User booker = createUser();
        Booking booking = createBooking(item, booker, false);

        when(bookingRepository.findById(anyInt()))
                .thenReturn(Optional.of(booking));
        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(owner));

        Booking updatedBooking = new Booking();
        updatedBooking.setId(booking.getId());
        updatedBooking.setItem(booking.getItem());
        updatedBooking.setBooker(booking.getBooker());
        updatedBooking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        updatedBooking.setStart(booking.getStart());
        updatedBooking.setEnd(booking.getEnd());

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(updatedBooking);

        BookingDto result = bookingService.changeBookingStatus(owner.getId(), booking.getId(), approved);

        assertEquals(updatedBooking.getId(), result.getId());
        assertEquals(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(updatedBooking.getStart()), result.getStart());
        assertEquals(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(updatedBooking.getEnd()), result.getEnd());
        assertEquals(updatedBooking.getItem().getId(), result.getItem().getId());
        assertEquals(updatedBooking.getBooker().getId(), result.getBooker().getId());
        assertEquals(updatedBooking.getStatus().name(), result.getStatus());
    }

    @Test
    void shouldNotChangeBookingStatusByUnknownUser() {
        User owner = createUser();
        Item item = createItem(owner);
        User booker = createUser();
        Booking booking = createBooking(item, booker, false);

        when(bookingRepository.findById(anyInt()))
                .thenReturn(Optional.of(booking));

        assertThrows(NoAccessException.class,
                () -> bookingService.changeBookingStatus(999, booking.getId(), false));
    }

    @Test
    void shouldNotChangeBookingStatusIfBookingNotFound() {
        when(bookingRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.changeBookingStatus(1, 1, false));
    }

    @Test
    void shouldNotChangeBookingStatusIfOwnerNotFound() {
        User owner = createUser();
        Item item = createItem(owner);
        User booker = createUser();
        Booking booking = createBooking(item, booker, false);

        when(bookingRepository.findById(anyInt()))
                .thenReturn(Optional.of(booking));
        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.changeBookingStatus(owner.getId(), booking.getId(), false));
    }

    @Test
    void shouldFindBookingByIdForOwner() {
        User owner = createUser();
        Item item = createItem(owner);
        User booker = createUser();
        Booking booking = createBooking(item, booker, false);

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findById(anyInt()))
                .thenReturn(Optional.of(booking));

        BookingDto result = bookingService.findById(owner.getId(), booking.getId());

        assertEquals(booking.getId(), result.getId());
        assertEquals(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(booking.getStart()), result.getStart());
        assertEquals(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(booking.getEnd()), result.getEnd());
        assertEquals(booking.getItem().getId(), result.getItem().getId());
        assertEquals(booking.getBooker().getId(), result.getBooker().getId());
        assertEquals(booking.getStatus().name(), result.getStatus());
    }

    @Test
    void shouldFindBookingByIdForBooker() {
        User owner = createUser();
        Item item = createItem(owner);
        User booker = createUser();
        Booking booking = createBooking(item, booker, false);

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(booker));
        when(bookingRepository.findById(anyInt()))
                .thenReturn(Optional.of(booking));

        BookingDto result = bookingService.findById(booker.getId(), booking.getId());

        assertEquals(booking.getId(), result.getId());
        assertEquals(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(booking.getStart()), result.getStart());
        assertEquals(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(booking.getEnd()), result.getEnd());
        assertEquals(booking.getItem().getId(), result.getItem().getId());
        assertEquals(booking.getBooker().getId(), result.getBooker().getId());
        assertEquals(booking.getStatus().name(), result.getStatus());
    }

    @Test
    void shouldNotFindBookingByIdForUnknownUser() {
        User owner = createUser();
        Item item = createItem(owner);
        User booker = createUser();
        Booking booking = createBooking(item, booker, false);
        User someSuspiciousPerson = createUser();

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(someSuspiciousPerson));
        when(bookingRepository.findById(anyInt()))
                .thenReturn(Optional.of(booking));

        assertThrows(NoAccessException.class,
                () -> bookingService.findById(someSuspiciousPerson.getId(), booking.getId()));
    }

    @Test
    void shouldNotFindBookingByIdIfUserNotFound() {
        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.findById(999, 999));
    }

    @Test
    void shouldNotFindUnknownBookingById() {
        User watcher = createUser();

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(watcher));
        when(bookingRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.findById(watcher.getId(), 999));
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void shouldFindAllBookingsForBooker(BookingState state) {
        User booker = createUser();
        List<Booking> bookings = List.of(createBooking(createItem(), booker, true),
                createBooking(createItem(), booker, false));

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdOrderByStartDesc(anyInt()))
                .thenReturn(bookings);
        when(bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(anyInt(), any(LocalDateTime.class)))
                .thenReturn(bookings);
        when(bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(anyInt(), any(LocalDateTime.class)))
                .thenReturn(bookings);
        when(bookingRepository.findCurrentByBookerId(anyInt(), any(LocalDateTime.class)))
                .thenReturn(bookings);
        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(anyInt(), anyString()))
                .thenReturn(bookings);

        List<BookingDto> result = bookingService.findAllByBookerId(booker.getId(), state.name());

        assertEquals(bookings.size(), result.size());
    }

    @Test
    void shouldNotReturnAllBookingsForBookerIfBookerNotFound() {
        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.findAllByBookerId(999, "state"));
    }

    @Test
    void shouldNotReturnAllBookingsForBookerIfBookingStateIsInvalid() {
        User booker = createUser();

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(booker));

        assertThrows(ArgumentException.class, () -> bookingService.findAllByBookerId(booker.getId(), "invalid"));
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void shouldFindAllBookingsForOwner(BookingState state) {
        User owner = createUser();

        List<Booking> bookings = List.of(createBooking(createItem(owner), createUser(), true),
                createBooking(createItem(owner), createUser(), false));

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(anyInt()))
                .thenReturn(bookings);
        when(bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(anyInt(), any(LocalDateTime.class)))
                .thenReturn(bookings);
        when(bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(anyInt(), any(LocalDateTime.class)))
                .thenReturn(bookings);
        when(bookingRepository.findCurrentByOwnerId(anyInt(), any(LocalDateTime.class)))
                .thenReturn(bookings);
        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(anyInt(), anyString()))
                .thenReturn(bookings);

        List<BookingDto> result = bookingService.findAllByOwnerId(owner.getId(), state.name());

        assertEquals(bookings.size(), result.size());
    }

    @Test
    void shouldNotReturnAllBookingsForOwnerIfOwnerNotFound() {
        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.findAllByOwnerId(999, "state"));
    }

    @Test
    void shouldNotReturnAllBookingsForOwnerIfBookingStateIsInvalid() {
        User owner = createUser();

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(owner));

        assertThrows(ArgumentException.class, () -> bookingService.findAllByOwnerId(owner.getId(), "invalid"));
    }

    private Item createItem(User owner) {
        Item item = new Item();

        item.setId(random.nextInt(100));
        item.setName(RandomUtils.createName());
        item.setDescription(RandomUtils.createName(50));
        item.setAvailable(true);
        item.setOwner(owner);

        return item;
    }

    private Booking createBooking(Item item, User booker, PostBookingRequest request) {
        Booking booking = new Booking();

        booking.setId(random.nextInt(100));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        booking.setStart(LocalDateTime.parse(request.getStart(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        booking.setEnd(LocalDateTime.parse(request.getEnd(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return booking;
    }

    private Booking createBooking(Item item, User booker, boolean isCompleted) {
        Booking booking = new Booking();

        booking.setId(random.nextInt(100));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        if (isCompleted) {
            booking.setStart(LocalDateTime.now().minusHours(random.nextLong(5, 20)));
        } else {
            booking.setStart(LocalDateTime.now().plusMinutes(random.nextLong(1, 100)));
        }

        booking.setEnd(booking.getStart().plusHours(1));

        return booking;
    }

    private Item createItem(int id) {
        Item item = new Item();

        item.setId(id);
        item.setName(RandomUtils.createName());
        item.setDescription(RandomUtils.createName(50));
        item.setAvailable(true);
        item.setOwner(createUser());

        return item;
    }

    private Item createItem() {
        return createItem(random.nextInt(100));
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