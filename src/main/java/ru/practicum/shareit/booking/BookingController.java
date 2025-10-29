package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.PostBookingRequest;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto createBooking(@RequestHeader("X-Sharer-User-Id") @Positive Integer bookerId,
                                    @Valid @RequestBody PostBookingRequest request) {
        log.debug("POST /bookings");
        log.debug("X-Sharer-User-Id = {}", bookerId);
        return bookingService.createBooking(bookerId, request);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto changeStatus(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId,
                                   @PathVariable @Positive int bookingId,
                                   @RequestParam boolean approved) {
        log.debug("PATCH /bookings/{bookingId}?approved={}", approved);
        log.debug("X-Sharer-User-Id = {}", userId);
        return bookingService.changeBookingStatus(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId,
                                 @PathVariable @Positive int bookingId) {
        log.debug("GET /bookings/{}", bookingId);
        log.debug("X-Sharer-User-Id = {}", userId);
        return bookingService.findById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getAllUsersBookings(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId,
                                                @RequestParam(required = false, defaultValue = "ALL") String state) {
        log.debug("GET /bookings?state={}", state);
        log.debug("X-Sharer-User-Id = {}", userId);
        return bookingService.findAllByBookerId(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllOwnersBookings(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId,
                                                 @RequestParam(required = false, defaultValue = "ALL") String state) {
        log.debug("GET /bookings/owner?state={}", state);
        log.debug("X-Sharer-User-Id = {}", userId);
        return bookingService.findAllByOwnerId(userId, state);
    }
}
