package ru.practicum.shareit.server.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.booking.dto.BookingDto;
import ru.practicum.shareit.server.booking.model.BookingState;
import ru.practicum.shareit.server.item.ItemService;
import ru.practicum.shareit.server.item.ItemServiceImpl;
import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.user.UserService;
import ru.practicum.shareit.server.user.UserServiceImpl;
import ru.practicum.shareit.server.user.dto.UserDto;
import ru.practicum.shareit.server.utils.BookingTestData;
import ru.practicum.shareit.server.utils.ItemTestData;
import ru.practicum.shareit.server.utils.UserTestData;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@Transactional
@DataJpaTest
@Import(value = {ItemServiceImpl.class, UserServiceImpl.class, BookingServiceImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"/schema.sql", "/clear.sql"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplIntegrationTest {
    private final BookingService bookingService;
    private final ItemService itemService;
    private final UserService userService;

    @Test
    void shouldFindAllBookersBookingsWithGivenState() {
        // создадим по одному бронированию для каждого значения BookingState
        UserDto booker = userService.createUser(UserTestData.createNewUserDto());

        // PAST
        UserDto owner = userService.createUser(UserTestData.createNewUserDto());
        ItemDto item = itemService.createItem(owner.getId(), ItemTestData.createNewItemDto());
        BookingDto booking = bookingService.createBooking(booker.getId(),
                BookingTestData.createNewBookingDto(item, true));
        bookingService.changeBookingStatus(owner.getId(), booking.getId(), true);

        // CURRENT
        owner = userService.createUser(UserTestData.createNewUserDto());
        item = itemService.createItem(owner.getId(), ItemTestData.createNewItemDto());
        booking = bookingService.createBooking(booker.getId(),
                BookingTestData.createNewBookingDto(item, LocalDateTime.now().minusHours(1),
                        LocalDateTime.now().plusHours(1)));
        bookingService.changeBookingStatus(owner.getId(), booking.getId(), true);

        // FUTURE
        owner = userService.createUser(UserTestData.createNewUserDto());
        item = itemService.createItem(owner.getId(), ItemTestData.createNewItemDto());
        booking = bookingService.createBooking(booker.getId(),
                BookingTestData.createNewBookingDto(item, false));
        bookingService.changeBookingStatus(owner.getId(), booking.getId(), true);

        // WAITING
        owner = userService.createUser(UserTestData.createNewUserDto());
        item = itemService.createItem(owner.getId(), ItemTestData.createNewItemDto());
        bookingService.createBooking(booker.getId(),
                BookingTestData.createNewBookingDto(item, false));

        // REJECTED
        owner = userService.createUser(UserTestData.createNewUserDto());
        item = itemService.createItem(owner.getId(), ItemTestData.createNewItemDto());
        // полагаем, что дата начала отменённого бронирования ещё не наступила
        booking = bookingService.createBooking(booker.getId(),
                BookingTestData.createNewBookingDto(item, false));
        bookingService.changeBookingStatus(owner.getId(), booking.getId(), false);

        // запросим бронирования
        // ALL
        List<BookingDto> bookings = bookingService.findAllByBookerId(booker.getId(), BookingState.ALL.name());
        assertEquals(5, bookings.size());

        // PAST
        bookings = bookingService.findAllByBookerId(booker.getId(), BookingState.PAST.name());
        assertEquals(1, bookings.size());

        // CURRENT
        bookings = bookingService.findAllByBookerId(booker.getId(), BookingState.CURRENT.name());
        assertEquals(1, bookings.size());

        // FUTURE
        bookings = bookingService.findAllByBookerId(booker.getId(), BookingState.FUTURE.name());
        assertEquals(3, bookings.size()); // сюда также попадут WAITING и REJECTED

        // WAITING
        bookings = bookingService.findAllByBookerId(booker.getId(), BookingState.WAITING.name());
        assertEquals(1, bookings.size());

        // REJECTED
        bookings = bookingService.findAllByBookerId(booker.getId(), BookingState.REJECTED.name());
        assertEquals(1, bookings.size());
    }
}