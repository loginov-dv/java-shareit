package ru.practicum.shareit.gateway.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.gateway.booking.dto.BookingDto;
import ru.practicum.shareit.gateway.booking.dto.PostBookingRequest;
import ru.practicum.shareit.gateway.booking.model.BookingStatus;
import ru.practicum.shareit.gateway.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BookingTestData {
    // с рандомным id
    public static BookingDto createBookingDto(PostBookingRequest request, int bookerId, BookingStatus status) {
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
    public static BookingDto createBookingDto(PostBookingRequest request, BookingStatus status) {
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
    public static PostBookingRequest createPostBookingRequest() {
        PostBookingRequest request = new PostBookingRequest();

        request.setItemId(new Random().nextInt(100));
        request.setStart(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusHours(1)));
        request.setEnd(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusHours(2)));

        return request;
    }
}
