package ru.practicum.shareit.gateway.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.practicum.shareit.gateway.booking.dto.NewBookingDto;
import ru.practicum.shareit.gateway.booking.model.BookingState;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createBooking(@RequestHeader("X-Sharer-User-Id") @Positive Integer bookerId,
                                                @Valid @RequestBody NewBookingDto request) {
        log.debug("gateway: POST /bookings");
        log.debug("gateway: X-Sharer-User-Id = {}", bookerId);

        validateBookingDates(request);

        return bookingClient.createBooking(bookerId, request);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> changeStatus(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId,
                                   @PathVariable @Positive int bookingId,
                                   @RequestParam boolean approved) {
        log.debug("gateway: PATCH /bookings/{}?approved={}", bookingId, approved);
        log.debug("gateway: X-Sharer-User-Id = {}", userId);
        return bookingClient.changeBookingStatus(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId,
                                 @PathVariable @Positive int bookingId) {
        log.debug("gateway: GET /bookings/{}", bookingId);
        log.debug("gateway: X-Sharer-User-Id = {}", userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUsersBookings(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId,
                                                      @RequestParam(required = false, defaultValue = "ALL") String stateParam) {
        log.debug("gateway: GET /bookings?state={}", stateParam);
        log.debug("gateway: X-Sharer-User-Id = {}", userId);

        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Некорректное значение статуса для запроса бронирований: " + stateParam));

        return bookingClient.getAllBookersBookings(userId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllOwnersBookings(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId,
                                                       @RequestParam(required = false, defaultValue = "ALL") String stateParam) {
        log.debug("gateway: GET /bookings/owner?state={}", stateParam);
        log.debug("gateway: X-Sharer-User-Id = {}", userId);

        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Некорректное значение статуса для запроса бронирований: " + stateParam));

        return bookingClient.getAllOwnersBookings(userId, state);
    }

    private void validateBookingDates(NewBookingDto request) {
        LocalDateTime start = request.getStart();
        LocalDateTime end = request.getEnd();

        if (start.isAfter(end)) {
            log.warn("Дата окончания бронирования должна быть после даты начала бронирования");
            throw new IllegalArgumentException("Дата окончания бронирования должна быть " +
                    "после даты начала бронирования");
        }

        if (start.equals(end)) {
            log.warn("Дата окончания бронирования не может совпадать с датой начала бронирования");
            throw new IllegalArgumentException("Дата окончания бронирования не может " +
                    "совпадать с датой начала бронирования");
        }

        log.debug("gateway: Валидация дат бронирования завершена успешно");
    }
}
