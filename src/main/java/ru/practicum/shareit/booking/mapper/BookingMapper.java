package ru.practicum.shareit.booking.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.PostBookingRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BookingMapper {

    public static Booking toNewBooking(User booker, Item item, PostBookingRequest request) {
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
        dto.setItem(ItemMapper.toItemShortDto(booking.getItem()));
        dto.setBooker(UserMapper.toUserDto(booking.getBooker()));
        dto.setStatus(booking.getStatus().name());
        dto.setStart(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(booking.getStart()));
        dto.setEnd(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(booking.getEnd()));

        return dto;
    }
}
