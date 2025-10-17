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
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        log.debug("Запрос на бронирование предмета с id = {} от пользователя с id = {}: {}",
                bookerId, request.getItemId(), request);

        User booker = findAndGetUser(bookerId);
        Optional<Item> maybeItem = itemRepository.findById(request.getItemId());

        if (maybeItem.isEmpty()) {
            log.warn(LogConstants.ITEM_NOT_FOUND_BY_ID, request.getItemId());
            throw new NotFoundException(String.format(ExceptionConstants.ITEM_NOT_FOUND_BY_ID, request.getItemId()));
        }

        Item item = maybeItem.get();

        if (!item.isAvailable()) {
            log.warn(LogConstants.ITEM_NOT_AVAILABLE);
            throw new NotAvailableException(ExceptionConstants.ITEM_NOT_AVAILABLE);
        }

        Booking booking = BookingMapper.toNewBooking(booker, item, request);

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

        Booking booking = maybeBooking.get();

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            log.warn(LogConstants.NO_ACCESS_FOR_BOOKING_APPROVAL);
            throw new NoAccessException(ExceptionConstants.NO_ACCESS_FOR_BOOKING_APPROVAL);
        }

        Optional<User> maybeUser = userRepository.findById(userId);

        if (maybeUser.isEmpty()) {
            log.warn(LogConstants.USER_NOT_FOUND_BY_ID, userId);
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, userId));
        }

        BookingStatus newStatus = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        booking.setStatus(newStatus);
        booking = bookingRepository.save(booking);

        log.debug("Изменён статус бронирования на {}", newStatus);

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto findById(int userId, int bookingId) {
        log.debug("Запрос на получение бронирования с id = {} от пользователя с id = {}", bookingId, userId);

        User user = findAndGetUser(userId);

        Optional<Booking> maybeBooking = bookingRepository.findById(bookingId);

        if (maybeBooking.isEmpty()) {
            log.warn(LogConstants.BOOKING_NOT_FOUND_BY_ID, bookingId);
            throw new NotFoundException(String.format(ExceptionConstants.BOOKING_NOT_FOUND_BY_ID, bookingId));
        }

        Booking booking = maybeBooking.get();

        if (!user.getId().equals(booking.getItem().getOwner().getId())
            && !user.getId().equals(booking.getBooker().getId())) {
            log.warn(LogConstants.NO_ACCESS_TO_VIEW_BOOKING);
            throw new NoAccessException(ExceptionConstants.NO_ACCESS_TO_VIEW_BOOKING);
        }

        log.debug("Бронирование: {}", booking);

        return BookingMapper.toBookingDto(booking);
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

        BookingState bookingState;

        try {
            bookingState = BookingState.valueOf(state);
        }
        catch (IllegalArgumentException ex) {
            log.warn(LogConstants.INVALID_BOOKING_STATE);
            throw new ArgumentException(ExceptionConstants.INVALID_BOOKING_STATE);
        }

        List<Booking> bookings = new ArrayList<>();

        switch (bookingState) {
            case ALL: {
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(bookerId);
                break;
            }
            case PAST: {
                bookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(bookerId, LocalDateTime.now());
                break;
            }
            case FUTURE: {
                bookings = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(bookerId, LocalDateTime.now());
                break;
            }
            case CURRENT: {
                bookings = bookingRepository.findCurrentByBookerId(bookerId, LocalDateTime.now());
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
        log.debug("Запрос всех бронирований владельца предметов с id = {} со статусом {}", ownerId, state);

        Optional<User> maybeUser = userRepository.findById(ownerId);

        if (maybeUser.isEmpty()) {
            log.warn(LogConstants.USER_NOT_FOUND_BY_ID, ownerId);
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, ownerId));
        }

        BookingState bookingState;

        try {
            bookingState = BookingState.valueOf(state);
        }
        catch (IllegalArgumentException ex) {
            log.warn(LogConstants.INVALID_BOOKING_STATE);
            throw new ArgumentException(ExceptionConstants.INVALID_BOOKING_STATE);
        }

        List<Booking> bookings = new ArrayList<>();

        switch (bookingState) {
            case ALL: {
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(ownerId);
                break;
            }
            case PAST: {
                bookings = bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(ownerId, LocalDateTime.now());
                break;
            }
            case FUTURE: {
                bookings = bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(ownerId, LocalDateTime.now());
                break;
            }
            case CURRENT: {
                bookings = bookingRepository.findCurrentByOwnerId(ownerId, LocalDateTime.now());
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

        if (booking.getStart().isBefore(LocalDateTime.now())) {
            log.warn(LogConstants.START_DATE_IN_THE_PAST);
            throw new BookingDateException(ExceptionConstants.START_DATE_IN_THE_PAST);
        }

        if (booking.getEnd().isBefore(LocalDateTime.now())) {
            log.warn(LogConstants.END_DATE_IN_THE_PAST);
            throw new BookingDateException(ExceptionConstants.END_DATE_IN_THE_PAST);
        }

        log.debug("Валидация дат бронирования завершена успешно");
    }

    private User findAndGetUser(int userId) {
        Optional<User> maybeUser = userRepository.findById(userId);

        if (maybeUser.isEmpty()) {
            log.warn(LogConstants.USER_NOT_FOUND_BY_ID, userId);
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, userId));
        }

        return maybeUser.get();
    }
}
