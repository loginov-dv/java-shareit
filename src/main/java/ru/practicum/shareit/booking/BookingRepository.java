package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(int bookerId, BookingStatus status);

    List<Booking> findByBookerIdOrderByStartDesc(int bookerId);

    // Запрос завершённых бронирований
    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(int bookerId, LocalDateTime end);

    // Запрос предстоящих бронирований
    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(int bookerId, LocalDateTime start);

    @Query("SELECT booking FROM Booking booking WHERE " +
            "booking.booker.id = ?1 AND (?2 BETWEEN booking.start AND booking.end) " +
            "ORDER BY booking.start DESC")
    List<Booking> findCurrentByBookerId(int bookerId, LocalDateTime now);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(int ownerId, BookingStatus status);

    List<Booking> findByItemOwnerIdOrderByStartDesc(int ownerId);

    // Запрос завершённых бронирований
    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(int ownerId, LocalDateTime end);

    // Запрос предстоящих бронирований
    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(int ownerId, LocalDateTime start);

    @Query("SELECT booking FROM Booking booking WHERE " +
            "booking.item.owner.id = ?1 AND (?2 BETWEEN booking.start AND booking.end) " +
            "ORDER BY booking.start DESC")
    List<Booking> findCurrentByOwnerId(int ownerId, LocalDateTime now);

    List<Booking> findByItemIdOrderByStart(int itemId);

    List<Booking> findByItemIdInOrderByStart(Collection<Integer> itemIds);
}
