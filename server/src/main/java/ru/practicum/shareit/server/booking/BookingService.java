package ru.practicum.shareit.server.booking;

import ru.practicum.shareit.server.booking.dto.BookingDto;
import ru.practicum.shareit.server.booking.dto.NewBookingDto;

import java.util.List;

public interface BookingService {

    BookingDto createBooking(int bookerId, NewBookingDto dto);

    BookingDto changeBookingStatus(int userId, int bookingId, boolean approved);

    BookingDto findById(int userId, int bookingId);

    List<BookingDto> findAllByBookerId(int bookerId, String state);

    List<BookingDto> findAllByOwnerId(int ownerId, String state);
}
