package ru.practicum.shareit.server.booking.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import ru.practicum.shareit.server.booking.dto.BookingDto;
import ru.practicum.shareit.server.booking.dto.BookingShortDto;
import ru.practicum.shareit.server.booking.dto.NewBookingDto;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.item.mapper.ItemMapper;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.user.mapper.UserMapper;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BookingMapper {

    public static Booking toNewBooking(User booker, Item item, NewBookingDto request) {
        Booking booking = new Booking();

        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(LocalDateTime.parse(request.getStart(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        booking.setEnd(LocalDateTime.parse(request.getEnd(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return booking;
    }

    public static BookingDto toBookingDto(Booking booking) {
        BookingDto dto = new BookingDto();

        dto.setId(booking.getId());
        dto.setItem(ItemMapper.toItemDto(booking.getItem()));
        dto.setBooker(UserMapper.toUserDto(booking.getBooker()));
        dto.setStatus(booking.getStatus().name());
        dto.setStart(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(booking.getStart()));
        dto.setEnd(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(booking.getEnd()));

        return dto;
    }

    public static BookingShortDto toBookingShortDto(Booking booking) {
        BookingShortDto dto = new BookingShortDto();

        dto.setId(booking.getId());
        dto.setStatus(booking.getStatus().name());
        dto.setStart(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(booking.getStart()));
        dto.setEnd(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(booking.getEnd()));

        return dto;
    }
}
