package ru.practicum.shareit.gateway.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.gateway.booking.dto.BookingDto;
import ru.practicum.shareit.gateway.booking.dto.NewBookingDto;
import ru.practicum.shareit.gateway.booking.model.BookingStatus;
import ru.practicum.shareit.gateway.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BookingTestData {
    // с рандомным id
    public static BookingDto createBookingDto(NewBookingDto request, int bookerId, BookingStatus status) {
        BookingDto bookingDto = new BookingDto();

        bookingDto.setId(new Random().nextInt(100));

        bookingDto.setStart(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(request.getStart()));
        bookingDto.setEnd(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(request.getEnd()));
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

        bookingDto.setStart(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(request.getStart()));
        bookingDto.setEnd(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(request.getEnd()));
        bookingDto.setStatus(status.name());

        ItemDto itemDto = ItemTestData.createItemDto(request.getItemId());
        bookingDto.setItem(itemDto);

        bookingDto.setBooker(UserTestData.createUserDto());

        return bookingDto;
    }

    public static BookingDto createBookingDto(BookingStatus status) {
        return createBookingDto(createNewBookingDto(), status);
    }

    // с рандомным id предмета
    public static NewBookingDto createNewBookingDto() {
        NewBookingDto request = new NewBookingDto();

        request.setItemId(new Random().nextInt(100));
        request.setStart(LocalDateTime.now().plusHours(1));
        request.setEnd(LocalDateTime.now().plusHours(2));

        return request;
    }
}
