package ru.practicum.shareit.server.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import ru.practicum.shareit.server.booking.dto.BookingDto;
import ru.practicum.shareit.server.booking.dto.PostBookingRequest;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.model.BookingState;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.exception.*;
import ru.practicum.shareit.server.item.ItemRepository;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.user.UserRepository;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.utils.BookingTestData;
import ru.practicum.shareit.server.utils.ItemTestData;
import ru.practicum.shareit.server.utils.UserTestData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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

    @Test
    void shouldCreateBooking() {
        Item item = ItemTestData.createItem(UserTestData.createUser());
        User booker = UserTestData.createUser();

        PostBookingRequest request = BookingTestData.createPostBookingRequest(item);

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.of(item));

        Booking booking = BookingTestData.createBooking(item, booker, request);

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
        Item item = ItemTestData.createItem(UserTestData.createUser());

        PostBookingRequest request = BookingTestData.createPostBookingRequest(item);

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(999, request));
    }

    @Test
    void shouldNotCreateBookingOfUnknownItem() {
        User booker = UserTestData.createUser();

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

    @Test
    void shouldNotCreateBookingIfItemIsUnavailable() {
        Item item = ItemTestData.createItem(UserTestData.createUser());
        item.setAvailable(false);

        User booker = UserTestData.createUser();

        PostBookingRequest request = BookingTestData.createPostBookingRequest(item);

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyInt()))
                .thenReturn(Optional.of(item));

        assertThrows(NotAvailableException.class, () -> bookingService.createBooking(booker.getId(), request));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldChangeBookingStatusByOwner(boolean approved) {
        User owner = UserTestData.createUser();
        Item item = ItemTestData.createItem(owner);
        User booker = UserTestData.createUser();
        Booking booking = BookingTestData.createBooking(item, booker, false);

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
        User owner = UserTestData.createUser();
        Item item = ItemTestData.createItem(owner);
        User booker = UserTestData.createUser();
        Booking booking = BookingTestData.createBooking(item, booker, false);

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
        User owner = UserTestData.createUser();
        Item item = ItemTestData.createItem(owner);
        User booker = UserTestData.createUser();
        Booking booking = BookingTestData.createBooking(item, booker, false);

        when(bookingRepository.findById(anyInt()))
                .thenReturn(Optional.of(booking));
        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.changeBookingStatus(owner.getId(), booking.getId(), false));
    }

    @Test
    void shouldFindBookingByIdForOwner() {
        User owner = UserTestData.createUser();
        Item item = ItemTestData.createItem(owner);
        User booker = UserTestData.createUser();
        Booking booking = BookingTestData.createBooking(item, booker, false);

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
        User owner = UserTestData.createUser();
        Item item = ItemTestData.createItem(owner);
        User booker = UserTestData.createUser();
        Booking booking = BookingTestData.createBooking(item, booker, false);

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
        User owner = UserTestData.createUser();
        Item item = ItemTestData.createItem(owner);
        User booker = UserTestData.createUser();
        Booking booking = BookingTestData.createBooking(item, booker, false);

        User someSuspiciousPerson = UserTestData.createUser();

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
        User watcher = UserTestData.createUser();

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
        User booker = UserTestData.createUser();
        List<Booking> bookings = List.of(
                BookingTestData.createBooking(ItemTestData.createItem(UserTestData.createUser()), booker, true),
                BookingTestData.createBooking(ItemTestData.createItem(UserTestData.createUser()), booker, false)
        );

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
        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(anyInt(), any()))
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
        User booker = UserTestData.createUser();

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(booker));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.findAllByBookerId(booker.getId(), "invalid"));
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void shouldFindAllBookingsForOwner(BookingState state) {
        User owner = UserTestData.createUser();

        List<Booking> bookings = List.of(
                BookingTestData.createBooking(ItemTestData.createItem(owner), UserTestData.createUser(), true),
                BookingTestData.createBooking(ItemTestData.createItem(owner), UserTestData.createUser(), false)
        );

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
        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(anyInt(), any()))
                .thenReturn(bookings);

        List<BookingDto> result = bookingService.findAllByOwnerId(owner.getId(), state.name());

        assertEquals(bookings.size(), result.size());
    }

    @Test
    void shouldNotReturnAllBookingsForOwnerIfOwnerNotFound() {
        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.findAllByOwnerId(999, "state"));
    }

    @Test
    void shouldNotReturnAllBookingsForOwnerIfBookingStateIsInvalid() {
        User owner = UserTestData.createUser();

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(owner));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.findAllByOwnerId(owner.getId(), "invalid"));
    }
}