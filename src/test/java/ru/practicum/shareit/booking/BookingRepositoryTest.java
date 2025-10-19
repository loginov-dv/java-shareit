package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
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

    private Booking createBooking() {
        Booking booking = new Booking();

        booking.setStatus(BookingStatus.WAITING);
        booking.setStart(LocalDateTime.now().plusHours(1));
        booking.setEnd(LocalDateTime.now().plusHours(2));

        Item item = createItem();
        User booker = createUser();

        booking.setItem(item);
        booking.setBooker(booker);

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

    private User createUser() {
        User user = new User();
        String name = RandomUtils.createName();

        user.setName(name);
        user.setEmail(name + "@mail.ru");

        user = userRepository.save(user);

        return user;
    }
}