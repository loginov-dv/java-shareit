package ru.practicum.shareit.server.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.server.booking.dto.BookingDto;
import ru.practicum.shareit.server.booking.dto.NewBookingDto;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BookingTestData {
    // без id для сохранения в БД
    public static Booking createNewBooking(Item item, User booker) {
        return createNewBooking(item, booker, LocalDateTime.now().plusHours(1));
    }

    // без id для сохранения в БД
    public static Booking createNewBooking(Item item, User booker, LocalDateTime start) {
        Booking booking = new Booking();

        booking.setStatus(BookingStatus.WAITING);
        booking.setStart(start);
        booking.setEnd(start.plusHours(1));

        booking.setItem(item);
        booking.setBooker(booker);

        return booking;
    }

    // с рандомным id
    public static Booking createBooking(Item item, User booker, boolean isCompleted) {
        Booking booking = new Booking();
        Random random = new Random();

        booking.setId(random.nextInt(100));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        booking.setStart(isCompleted
                ? LocalDateTime.now().minusHours(random.nextLong(5, 20))
                : LocalDateTime.now().plusMinutes(random.nextLong(1, 100)));

        booking.setEnd(booking.getStart().plusHours(1));

        return booking;
    }

    public static Booking createBooking(Item item, User booker, NewBookingDto request) {
        Booking booking = new Booking();

        booking.setId(new Random().nextInt(100));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        booking.setStart(LocalDateTime.parse(request.getStart(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        booking.setEnd(LocalDateTime.parse(request.getEnd(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return booking;
    }

    // с рандомным id
    public static BookingDto createBookingDto(NewBookingDto request, int bookerId, BookingStatus status) {
        BookingDto bookingDto = new BookingDto();

        bookingDto.setId(new Random().nextInt(100));

        bookingDto.setStart(request.getStart());
        bookingDto.setEnd(request.getEnd());
        bookingDto.setStatus(status.name());

        ItemDto itemDto = ItemTestData.createItemDto(request.getItemId());
        bookingDto.setItem(itemDto);

        bookingDto.setBooker(UserTestData.createUserDto(bookerId));

        return bookingDto;
    }

    // с рандомным id и id букера
    public static BookingDto createBookingDto(NewBookingDto request, BookingStatus status) {
        BookingDto bookingDto = new BookingDto();

        bookingDto.setId(new Random().nextInt(100));

        bookingDto.setStart(request.getStart());
        bookingDto.setEnd(request.getEnd());
        bookingDto.setStatus(status.name());

        ItemDto itemDto = ItemTestData.createItemDto(request.getItemId());
        bookingDto.setItem(itemDto);

        bookingDto.setBooker(UserTestData.createUserDto());

        return bookingDto;
    }

    // с рандомным id предмета
    public static NewBookingDto createNewBookingDto() {
        NewBookingDto request = new NewBookingDto();

        request.setItemId(new Random().nextInt(100));
        request.setStart(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusHours(1)));
        request.setEnd(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusHours(2)));

        return request;
    }

    public static NewBookingDto createNewBookingDto(Item item) {
        NewBookingDto request = new NewBookingDto();

        request.setItemId(item.getId());
        request.setStart(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusHours(1)));
        request.setEnd(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusHours(2)));

        return request;
    }

    public static NewBookingDto createNewBookingDto(ItemDto itemDto, boolean isCompleted) {
        NewBookingDto request = new NewBookingDto();
        Random random = new Random();
        LocalDateTime now = LocalDateTime.now();

        request.setItemId(itemDto.getId());

        LocalDateTime start = isCompleted
                ? now.minusHours(random.nextLong(5, 20))
                : now.plusMinutes(random.nextLong(1, 100));

        request.setStart(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(start));
        request.setEnd(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(start.plusHours(1)));

        return request;
    }
}
