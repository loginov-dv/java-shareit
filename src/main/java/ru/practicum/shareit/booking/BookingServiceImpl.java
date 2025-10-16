package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.PostBookingRequest;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.BookingRequestState;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
// TODO: logs
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDto createBooking(int bookerId, PostBookingRequest request) {
        log.debug("Запрос на создание бронирования от пользователя с id = {} на предмет с id = {}: {}",
                bookerId, request.getItemId(), request);

        Optional<User> maybeUser = userRepository.findById(bookerId);

        if (maybeUser.isEmpty()) {
            log.warn(LogConstants.USER_NOT_FOUND_BY_ID, bookerId);
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, bookerId));
        }

        Optional<Item> maybeItem = itemRepository.findById(request.getItemId());

        if (maybeItem.isEmpty()) {
            log.warn(LogConstants.ITEM_NOT_FOUND_BY_ID, request.getItemId());
            throw new NotFoundException(String.format(ExceptionConstants.ITEM_NOT_FOUND_BY_ID, request.getItemId()));
        }

        if (!maybeItem.get().isAvailable()) {
            log.warn("Предмет недоступен для бронирования");
            throw new NotAvailableException("Предмет недоступен для бронирования");
        }

        Booking booking = BookingMapper.toNewBooking(maybeUser.get(), maybeItem.get(), request);

        validateBookingDates(booking);

        booking = bookingRepository.save(booking);
        log.debug("Добавлено бронирование: {}", booking);

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional
    public BookingDto changeBookingStatus(int userId, int bookingId, boolean approved) {
        log.debug("Запрос на изменение статуса бронирования с id = {} от пользователя с id = {}", bookingId, userId);

        Optional<Booking> maybeBooking = bookingRepository.findById(bookingId);

        if (maybeBooking.isEmpty()) {
            log.warn(LogConstants.BOOKING_NOT_FOUND_BY_ID, bookingId);
            throw new NotFoundException(String.format(ExceptionConstants.BOOKING_NOT_FOUND_BY_ID, bookingId));
        }

        if (!maybeBooking.get().getItem().getOwner().getId().equals(userId)) {
            log.warn("Нет доступа на изменение бронирования");
            throw new NoAccessException(ExceptionConstants.NO_ACCESS);
        }

        Optional<User> maybeUser = userRepository.findById(userId);

        if (maybeUser.isEmpty()) {
            log.warn(LogConstants.USER_NOT_FOUND_BY_ID, userId);
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, userId));
        }

        Booking booking = maybeBooking.get();

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        booking = bookingRepository.save(booking);

        log.debug("Изменён статус бронирования с id = {} на {}", bookingId, booking.getStatus().name());

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto findById(int userId, int bookingId) {
        log.debug("Запрос на получение бронирования с id = {} от пользователя с id = {}", bookingId, userId);

        Optional<User> maybeUser = userRepository.findById(userId);

        if (maybeUser.isEmpty()) {
            log.warn(LogConstants.USER_NOT_FOUND_BY_ID, userId);
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, userId));
        }

        Optional<Booking> maybeBooking = bookingRepository.findById(bookingId);

        if (maybeBooking.isEmpty()) {
            log.warn(LogConstants.BOOKING_NOT_FOUND_BY_ID, bookingId);
            throw new NotFoundException(String.format(ExceptionConstants.BOOKING_NOT_FOUND_BY_ID, bookingId));
        }

        if (!maybeUser.get().getId().equals(maybeBooking.get().getItem().getOwner().getId())
            && !maybeUser.get().getId().equals(maybeBooking.get().getBooker().getId())) {
            log.warn("Нет доступа на просмотр бронирования");
            throw new NoAccessException(ExceptionConstants.NO_ACCESS);
        }

        log.debug("Бронирование: {}", maybeBooking.get());

        return BookingMapper.toBookingDto(maybeBooking.get());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> findAllByBookerId(int bookerId, String state) {
        log.debug("Запрос всех бронирований пользователя с id = {} со статусом {}", bookerId, state);

        Optional<User> maybeUser = userRepository.findById(bookerId);

        if (maybeUser.isEmpty()) {
            log.warn(LogConstants.USER_NOT_FOUND_BY_ID, bookerId);
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, bookerId));
        }

        BookingRequestState status;
        try {
            status = BookingRequestState.valueOf(state);
        }
        catch (IllegalArgumentException ex) {
            log.warn(LogConstants.INVALID_BOOKING_STATE);
            throw new ArgumentException(ExceptionConstants.INVALID_BOOKING_STATE);
        }

        List<Booking> bookings = new ArrayList<>();

        switch (status) {
            case ALL: {
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(bookerId);
                break;
            }
            case PAST: {
                bookings = bookingRepository.findPastByBookerId(bookerId);
                break;
            }
            case FUTURE: {
                bookings = bookingRepository.findFutureByBookerId(bookerId);
                break;
            }
            case CURRENT: {
                bookings = bookingRepository.findCurrentByBookedId(bookerId);
                break;
            }
            case WAITING, REJECTED: {
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(bookerId, state);
                break;
            }
        }

        log.debug("Количество бронирований: {}", bookings.size());

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> findAllByOwnerId(int ownerId, String state) {
        log.debug("Запрос всех бронирований предметов владельца с id = {} со статусом {}", ownerId, state);

        Optional<User> maybeUser = userRepository.findById(ownerId);

        if (maybeUser.isEmpty()) {
            log.warn(LogConstants.USER_NOT_FOUND_BY_ID, ownerId);
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, ownerId));
        }

        BookingRequestState status;
        try {
            status = BookingRequestState.valueOf(state);
        }
        catch (IllegalArgumentException ex) {
            log.warn(LogConstants.INVALID_BOOKING_STATE);
            throw new ArgumentException(ExceptionConstants.INVALID_BOOKING_STATE);
        }

        List<Booking> bookings = new ArrayList<>();

        switch (status) {
            case ALL: {
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(ownerId);
                break;
            }
            case PAST: {
                bookings = bookingRepository.findPastByOwnerId(ownerId);
                break;
            }
            case FUTURE: {
                bookings = bookingRepository.findFutureByOwnerId(ownerId);
                break;
            }
            case CURRENT: {
                bookings = bookingRepository.findCurrentByOwnerId(ownerId);
                break;
            }
            case WAITING, REJECTED: {
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(ownerId, state);
                break;
            }
        }

        log.debug("Количество бронирований: {}", bookings.size());

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    private void validateBookingDates(Booking booking) {
        if (booking.getStart().isAfter(booking.getEnd())) {
            log.warn(LogConstants.INCORRECT_BOOKING_DATES_ORDER);
            throw new BookingDateException(ExceptionConstants.INCORRECT_BOOKING_DATES_ORDER);
        }

        if (booking.getStart().equals(booking.getEnd())) {
            log.warn(LogConstants.START_DATE_EQUALS_TO_END_DATE);
            throw new BookingDateException(ExceptionConstants.START_DATE_EQUALS_TO_END_DATE);
        }

        if (booking.getStart().isBefore(Instant.now())) {
            log.warn(LogConstants.START_DATE_IN_THE_PAST);
            throw new BookingDateException(ExceptionConstants.START_DATE_IN_THE_PAST);
        }

        if (booking.getEnd().isBefore(Instant.now())) {
            log.warn(LogConstants.END_DATE_IN_THE_PAST);
            throw new BookingDateException(ExceptionConstants.END_DATE_IN_THE_PAST);
        }

        log.debug("Валидация дат бронирования завершена успешно");
    }
}
