package ru.practicum.shareit.server.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ru.practicum.shareit.server.booking.dto.BookingDto;
import ru.practicum.shareit.server.booking.dto.NewBookingDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto createBooking(@RequestHeader("X-Sharer-User-Id") Integer bookerId,
                                    @RequestBody NewBookingDto request) {
        log.debug("server: POST /bookings");
        log.debug("server: X-Sharer-User-Id = {}", bookerId);
        return bookingService.createBooking(bookerId, request);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto changeStatus(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                   @PathVariable int bookingId,
                                   @RequestParam boolean approved) {
        log.debug("server: PATCH /bookings/{}?approved={}",bookingId, approved);
        log.debug("server: X-Sharer-User-Id = {}", userId);
        return bookingService.changeBookingStatus(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                 @PathVariable int bookingId) {
        log.debug("server: GET /bookings/{}", bookingId);
        log.debug("server: X-Sharer-User-Id = {}", userId);
        return bookingService.findById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getAllUsersBookings(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                                @RequestParam String state) {
        log.debug("server: GET /bookings?state={}", state);
        log.debug("server: X-Sharer-User-Id = {}", userId);
        return bookingService.findAllByBookerId(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllOwnersBookings(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                                 @RequestParam String state) {
        log.debug("server: GET /bookings/owner?state={}", state);
        log.debug("server: X-Sharer-User-Id = {}", userId);
        return bookingService.findAllByOwnerId(userId, state);
    }
}
