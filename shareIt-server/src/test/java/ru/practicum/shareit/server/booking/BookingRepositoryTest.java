package ru.practicum.shareit.server.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.item.ItemRepository;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.user.UserRepository;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.utils.BookingTestData;
import ru.practicum.shareit.server.utils.ItemTestData;
import ru.practicum.shareit.server.utils.UserTestData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"/schema.sql", "/clear.sql"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingRepositoryTest {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Test
    void shouldSaveBooking() {
        User owner = userRepository.save(UserTestData.createNewUser());
        Item item = itemRepository.save(ItemTestData.createNewItem(owner));
        User booker = userRepository.save(UserTestData.createNewUser());
        Booking booking = bookingRepository.save(BookingTestData.createNewBooking(item, booker));

        assertNotNull(booking.getId());
    }

    @Test
    void shouldFindBookingById() {
        User owner = userRepository.save(UserTestData.createNewUser());
        Item item = itemRepository.save(ItemTestData.createNewItem(owner));
        User booker = userRepository.save(UserTestData.createNewUser());
        Booking booking = bookingRepository.save(BookingTestData.createNewBooking(item, booker));
        Optional<Booking> maybeFoundBooking = bookingRepository.findById(booking.getId());

        if (maybeFoundBooking.isEmpty()) {
            fail();
        }

        Booking foundBooking = maybeFoundBooking.get();

        assertEquals(booking.getId(), foundBooking.getId());
        assertEquals(booking.getStart(), foundBooking.getStart());
        assertEquals(booking.getEnd(), foundBooking.getEnd());
        assertEquals(booking.getStatus(), foundBooking.getStatus());
        assertEquals(booking.getItem().getId(), foundBooking.getItem().getId());
        assertEquals(booking.getBooker().getId(), foundBooking.getBooker().getId());
    }

    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = {"APPROVED", "REJECTED", "CANCELED"})
    void shouldFindBookingsByBookerIdAndStatus(BookingStatus status) {
        User owner = userRepository.save(UserTestData.createNewUser());
        Item item1 = itemRepository.save(ItemTestData.createNewItem(owner));
        Item item2 = itemRepository.save(ItemTestData.createNewItem(owner));
        User booker = userRepository.save(UserTestData.createNewUser());

        // одно бронирование всегда со статусом WAITING
        Booking bookingWaiting = bookingRepository.save(BookingTestData.createNewBooking(item1, booker));
        // для второго бронирования задаём статус
        Booking bookingWithStatus = BookingTestData.createNewBooking(item2, booker);
        bookingWithStatus.setStatus(status);
        bookingWithStatus = bookingRepository.save(bookingWithStatus);

        List<Booking> bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(booker.getId(),
                status);

        assertEquals(1, bookings.size());
        assertTrue(bookings.contains(bookingWithStatus));
    }

    @Test
    void shouldFindBookingByBookerId() {
        User owner = userRepository.save(UserTestData.createNewUser());
        Item item = itemRepository.save(ItemTestData.createNewItem(owner));
        User booker = userRepository.save(UserTestData.createNewUser());
        Booking booking = bookingRepository.save(BookingTestData.createNewBooking(item, booker));
        List<Booking> bookings = bookingRepository.findByBookerIdOrderByStartDesc(booker.getId());

        assertEquals(1, bookings.size());
        assertTrue(bookings.contains(booking));
    }

    @Test
    void shouldFindPastBookingsForBooker() {
        User owner = userRepository.save(UserTestData.createNewUser());
        Item item1 = itemRepository.save(ItemTestData.createNewItem(owner));
        Item item2 = itemRepository.save(ItemTestData.createNewItem(owner));
        User booker = userRepository.save(UserTestData.createNewUser());

        Booking pastBooking = bookingRepository.save(BookingTestData.createNewBooking(item1, booker,
                LocalDateTime.now().minusHours(5)));
        Booking futureBooking = bookingRepository.save(BookingTestData.createNewBooking(item2, booker,
                LocalDateTime.now().plusHours(5)));

        List<Booking> bookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(booker.getId(),
                LocalDateTime.now());

        assertEquals(1, bookings.size());
        assertTrue(bookings.contains(pastBooking));
    }

    @Test
    void shouldFindFutureBookingsForBooker() {
        User owner = userRepository.save(UserTestData.createNewUser());
        Item item1 = itemRepository.save(ItemTestData.createNewItem(owner));
        Item item2 = itemRepository.save(ItemTestData.createNewItem(owner));
        User booker = userRepository.save(UserTestData.createNewUser());

        Booking pastBooking = bookingRepository.save(BookingTestData.createNewBooking(item1, booker,
                LocalDateTime.now().minusHours(5)));
        Booking futureBooking = bookingRepository.save(BookingTestData.createNewBooking(item2, booker,
                LocalDateTime.now().plusHours(5)));

        List<Booking> bookings = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(booker.getId(),
                LocalDateTime.now());

        assertEquals(1, bookings.size());
        assertTrue(bookings.contains(futureBooking));
    }

    @Test
    void shouldFindCurrentBookingsForBooker() {
        User owner = userRepository.save(UserTestData.createNewUser());
        Item item1 = itemRepository.save(ItemTestData.createNewItem(owner));
        Item item2 = itemRepository.save(ItemTestData.createNewItem(owner));
        Item item3 = itemRepository.save(ItemTestData.createNewItem(owner));
        User booker = userRepository.save(UserTestData.createNewUser());

        Booking pastBooking = bookingRepository.save(BookingTestData.createNewBooking(item1, booker,
                LocalDateTime.now().minusHours(5)));
        Booking currentBooking = bookingRepository.save(BookingTestData.createNewBooking(item2, booker,
                LocalDateTime.now().minusMinutes(30)));
        Booking futureBooking = bookingRepository.save(BookingTestData.createNewBooking(item3, booker,
                LocalDateTime.now().plusHours(5)));

        List<Booking> bookings = bookingRepository.findCurrentByBookerId(booker.getId(),
                LocalDateTime.now());

        assertEquals(1, bookings.size());
        assertTrue(bookings.contains(currentBooking));
    }

    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = {"APPROVED", "REJECTED", "CANCELED"})
    void shouldFindBookingsByOwnerIdAndStatus(BookingStatus status) {
        User owner = userRepository.save(UserTestData.createNewUser());
        Item item = itemRepository.save(ItemTestData.createNewItem(owner));
        User booker = userRepository.save(UserTestData.createNewUser());

        // одно бронирование всегда со статусом WAITING
        Booking bookingWaiting = bookingRepository.save(BookingTestData.createNewBooking(item, booker));
        // для второго бронирования задаём статус
        Booking bookingWithStatus = BookingTestData.createNewBooking(item, booker);
        bookingWithStatus.setStatus(status);
        bookingWithStatus = bookingRepository.save(bookingWithStatus);

        List<Booking> bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(owner.getId(),
                status);

        assertEquals(1, bookings.size());
        assertTrue(bookings.contains(bookingWithStatus));
    }

    @Test
    void shouldFindBookingByOwnerId() {
        User owner = userRepository.save(UserTestData.createNewUser());
        Item item = itemRepository.save(ItemTestData.createNewItem(owner));
        User booker = userRepository.save(UserTestData.createNewUser());
        Booking booking = bookingRepository.save(BookingTestData.createNewBooking(item, booker));

        List<Booking> bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(owner.getId());

        assertEquals(1, bookings.size());
        assertTrue(bookings.contains(booking));
    }

    @Test
    void shouldFindPastBookingsForOwner() {
        User owner = userRepository.save(UserTestData.createNewUser());
        Item item = itemRepository.save(ItemTestData.createNewItem(owner));
        User booker = userRepository.save(UserTestData.createNewUser());

        Booking pastBooking = bookingRepository.save(BookingTestData.createNewBooking(item, booker,
                LocalDateTime.now().minusHours(5)));
        Booking futureBooking = bookingRepository.save(BookingTestData.createNewBooking(item, booker,
                LocalDateTime.now().plusHours(5)));

        List<Booking> bookings = bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(owner.getId(),
                LocalDateTime.now());

        assertEquals(1, bookings.size());
        assertTrue(bookings.contains(pastBooking));
    }

    @Test
    void shouldFindFutureBookingsForOwner() {
        User owner = userRepository.save(UserTestData.createNewUser());
        Item item = itemRepository.save(ItemTestData.createNewItem(owner));
        User booker = userRepository.save(UserTestData.createNewUser());

        Booking pastBooking = bookingRepository.save(BookingTestData.createNewBooking(item, booker,
                LocalDateTime.now().minusHours(5)));
        Booking futureBooking = bookingRepository.save(BookingTestData.createNewBooking(item, booker,
                LocalDateTime.now().plusHours(5)));

        List<Booking> bookings = bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(owner.getId(),
                LocalDateTime.now());

        assertEquals(1, bookings.size());
        assertTrue(bookings.contains(futureBooking));
    }

    @Test
    void shouldFindCurrentBookingsForOwner() {
        User owner = userRepository.save(UserTestData.createNewUser());
        Item item = itemRepository.save(ItemTestData.createNewItem(owner));
        User booker = userRepository.save(UserTestData.createNewUser());

        Booking pastBooking = bookingRepository.save(BookingTestData.createNewBooking(item, booker,
                LocalDateTime.now().minusHours(5)));
        Booking currentBooking = bookingRepository.save(BookingTestData.createNewBooking(item, booker,
                LocalDateTime.now().minusMinutes(30)));
        Booking futureBooking = bookingRepository.save(BookingTestData.createNewBooking(item, booker,
                LocalDateTime.now().plusHours(5)));

        List<Booking> bookings = bookingRepository.findCurrentByOwnerId(owner.getId(),
                LocalDateTime.now());

        assertEquals(1, bookings.size());
        assertTrue(bookings.contains(currentBooking));
    }
}