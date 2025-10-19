package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.PostBookingRequest;

import java.util.List;

public interface BookingService {

    BookingDto createBooking(int bookerId, PostBookingRequest dto);

    BookingDto changeBookingStatus(int userId, int bookingId, boolean approved);

    BookingDto findById(int userId, int bookingId);

    List<BookingDto> findAllByBookerId(int bookerId, String state);

    List<BookingDto> findAllByOwnerId(int ownerId, String state);
}
