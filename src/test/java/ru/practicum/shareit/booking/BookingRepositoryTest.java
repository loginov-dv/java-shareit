package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utils.RandomUtils;

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
        Booking booking = createBooking();
        booking = bookingRepository.save(booking);

        assertNotNull(booking.getId());
    }

    @Test
    void shouldFindBookingById() {
        Booking booking = createBooking();
        booking = bookingRepository.save(booking);
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
        User booker = createUser();
        Booking bookingWaiting = createBooking(); // одно бронирование всегда со статусом WAITING
        bookingWaiting.setBooker(booker);
        Booking bookingWithStatus = createBooking();
        bookingWithStatus.setBooker(booker);

        bookingWithStatus.setStatus(status); // для второго бронирования задаём статус
        bookingWaiting = bookingRepository.save(bookingWaiting);
        bookingWithStatus = bookingRepository.save(bookingWithStatus);

        List<Booking> bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(booker.getId(),
                status);

        assertEquals(1, bookings.size());
        assertTrue(bookings.contains(bookingWithStatus));
    }

    @Test
    void shouldFindBookingByBookerId() {
        Booking booking = createBooking();

        booking = bookingRepository.save(booking);

        List<Booking> bookings = bookingRepository.findByBookerIdOrderByStartDesc(booking.getBooker().getId());

        assertEquals(1, bookings.size());
        assertTrue(bookings.contains(booking));
    }

    @Test
    void shouldFindPastBookingsForBooker() {
        User booker = createUser();
        Booking pastBooking = createBooking(LocalDateTime.now().minusHours(5));
        pastBooking.setBooker(booker);
        Booking futureBooking = createBooking(LocalDateTime.now().plusHours(5));
        pastBooking.setBooker(booker);

        pastBooking = bookingRepository.save(pastBooking);
        futureBooking = bookingRepository.save(futureBooking);

        List<Booking> bookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(booker.getId(),
                LocalDateTime.now());

        assertEquals(1, bookings.size());
        assertTrue(bookings.contains(pastBooking));
    }

    @Test
    void shouldFindFutureBookingsForBooker() {
        User booker = createUser();
        Booking pastBooking = createBooking(LocalDateTime.now().minusHours(5));
        pastBooking.setBooker(booker);
        Booking futureBooking = createBooking(LocalDateTime.now().plusHours(5));
        futureBooking.setBooker(booker);

        pastBooking = bookingRepository.save(pastBooking);
        futureBooking = bookingRepository.save(futureBooking);

        List<Booking> bookings = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(booker.getId(),
                LocalDateTime.now());

        assertEquals(1, bookings.size());
        assertTrue(bookings.contains(futureBooking));
    }

    @Test
    void shouldFindCurrentBookingsForBooker() {
        User booker = createUser();
        Booking pastBooking = createBooking(LocalDateTime.now().minusHours(5));
        pastBooking.setBooker(booker);
        Booking currentBooking = createBooking(LocalDateTime.now().minusMinutes(30));
        currentBooking.setBooker(booker);
        Booking futureBooking = createBooking(LocalDateTime.now().plusHours(5));
        pastBooking.setBooker(booker);

        pastBooking = bookingRepository.save(pastBooking);
        currentBooking = bookingRepository.save(currentBooking);
        futureBooking = bookingRepository.save(futureBooking);

        List<Booking> bookings = bookingRepository.findCurrentByBookerId(booker.getId(),
                LocalDateTime.now());

        assertEquals(1, bookings.size());
        assertTrue(bookings.contains(currentBooking));
    }

    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = {"APPROVED", "REJECTED", "CANCELED"})
    void shouldFindBookingsByOwnerIdAndStatus(BookingStatus status) {
        User owner = createUser();
        Item item = createItem(owner);

        Booking bookingWaiting = createBooking(item); // одно бронирование всегда со статусом WAITING
        Booking bookingWithStatus = createBooking(item);
        bookingWithStatus.setStatus(status); // для второго бронирования задаём статус
        bookingWaiting = bookingRepository.save(bookingWaiting);
        bookingWithStatus = bookingRepository.save(bookingWithStatus);

        List<Booking> bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(owner.getId(),
                status);

        assertEquals(1, bookings.size());
        assertTrue(bookings.contains(bookingWithStatus));
    }

    @Test
    void shouldFindBookingByOwnerId() {
        User owner = createUser();
        Item item = createItem(owner);
        Booking booking = createBooking(item);

        booking = bookingRepository.save(booking);

        List<Booking> bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(owner.getId());

        assertEquals(1, bookings.size());
        assertTrue(bookings.contains(booking));
    }

    @Test
    void shouldFindPastBookingsForOwner() {
        User owner = createUser();
        Item item = createItem(owner);

        Booking pastBooking = createBooking(item, LocalDateTime.now().minusHours(5));
        Booking futureBooking = createBooking(item, LocalDateTime.now().plusHours(5));

        pastBooking = bookingRepository.save(pastBooking);
        futureBooking = bookingRepository.save(futureBooking);

        List<Booking> bookings = bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(owner.getId(),
                LocalDateTime.now());

        assertEquals(1, bookings.size());
        assertTrue(bookings.contains(pastBooking));
    }

    @Test
    void shouldFindFutureBookingsForOwner() {
        User owner = createUser();
        Item item = createItem(owner);

        Booking pastBooking = createBooking(item, LocalDateTime.now().minusHours(5));
        Booking futureBooking = createBooking(item, LocalDateTime.now().plusHours(5));

        pastBooking = bookingRepository.save(pastBooking);
        futureBooking = bookingRepository.save(futureBooking);

        List<Booking> bookings = bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(owner.getId(),
                LocalDateTime.now());

        assertEquals(1, bookings.size());
        assertTrue(bookings.contains(futureBooking));
    }

    @Test
    void shouldFindCurrentBookingsForOwner() {
        User owner = createUser();
        Item item = createItem(owner);

        Booking pastBooking = createBooking(item, LocalDateTime.now().minusHours(5));
        Booking currentBooking = createBooking(item, LocalDateTime.now().minusMinutes(30));
        Booking futureBooking = createBooking(item, LocalDateTime.now().plusHours(5));

        pastBooking = bookingRepository.save(pastBooking);
        currentBooking = bookingRepository.save(currentBooking);
        futureBooking = bookingRepository.save(futureBooking);

        List<Booking> bookings = bookingRepository.findCurrentByOwnerId(owner.getId(),
                LocalDateTime.now());

        assertEquals(1, bookings.size());
        assertTrue(bookings.contains(currentBooking));
    }


    private Booking createBooking() {
        return createBooking(LocalDateTime.now().plusHours(1));
    }

    private Booking createBooking(Item item, LocalDateTime start) {
        Booking booking = new Booking();

        booking.setStatus(BookingStatus.WAITING);
        booking.setStart(start);
        booking.setEnd(booking.getStart().plusHours(1));

        booking.setItem(item);
        booking.setBooker(createUser());

        return booking;
    }

    private Booking createBooking(LocalDateTime start) {
        Booking booking = new Booking();

        booking.setStatus(BookingStatus.WAITING);
        booking.setStart(start);
        booking.setEnd(start.plusHours(1));

        Item item = createItem();
        User booker = createUser();

        booking.setItem(item);
        booking.setBooker(booker);

        return booking;
    }

    private Booking createBooking(Item item) {
        Booking booking = new Booking();

        booking.setStatus(BookingStatus.WAITING);
        booking.setStart(LocalDateTime.now().plusHours(1));
        booking.setEnd(booking.getStart().plusHours(1));

        booking.setItem(item);
        booking.setBooker(createUser());

        return booking;
    }

    private Item createItem() {
        User user = createUser();
        Item item = new Item();

        item.setName(RandomUtils.createName());
        item.setDescription(RandomUtils.createName(50));
        item.setAvailable(true);
        item.setOwner(user);

        item = itemRepository.save(item);

        return item;
    }

    private Item createItem(User owner) {
        Item item = new Item();

        item.setName(RandomUtils.createName());
        item.setDescription(RandomUtils.createName(50));
        item.setAvailable(true);
        item.setOwner(owner);

        item = itemRepository.save(item);

        return item;
    }

    private User createUser() {
        User user = new User();
        String name = RandomUtils.createName();

        user.setName(name);
        user.setEmail(name + "@mail.ru");

        user = userRepository.save(user);

        return user;
    }
}