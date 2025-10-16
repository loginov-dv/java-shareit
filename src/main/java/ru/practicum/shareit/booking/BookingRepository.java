package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(int bookedId, String status);

    List<Booking> findByBookerIdOrderByStartDesc(int bookerId);

    @Query("SELECT booking FROM Booking booking WHERE " +
            "booking.booker.id = ?1 AND booking.end < ?2 " +
            "ORDER BY booking.start DESC")
    List<Booking> findPastByBookerIdAndEndIsBefore(int bookerId, LocalDateTime end);

    @Query("SELECT booking FROM Booking booking WHERE " +
            "booking.booker.id = ?1 AND booking.start > ?2 " +
            "ORDER BY booking.start DESC")
    List<Booking> findFutureByBookerIdAndStartIsAfter(int bookerId, LocalDateTime start);

    @Query("SELECT booking FROM Booking booking WHERE " +
            "booking.booker.id = ?1 AND (?2 BETWEEN booking.start AND booking.end) " +
            "ORDER BY booking.start DESC")
    List<Booking> findCurrentByBookedId(int bookerId, LocalDateTime now);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(int ownerId, String status);

    List<Booking> findByItemOwnerIdOrderByStartDesc(int ownerId);

    @Query("SELECT booking FROM Booking booking WHERE " +
            "booking.item.owner.id = ?1 AND booking.end < ?2 " +
            "ORDER BY booking.start DESC")
    List<Booking> findPastByOwnerIdAndEndIsBefore(int ownerId, LocalDateTime end);

    @Query("SELECT booking FROM Booking booking WHERE " +
            "booking.item.owner.id = ?1 AND booking.start > ?2 " +
            "ORDER BY booking.start DESC")
    List<Booking> findFutureByOwnerIdAndStartIsAfter(int ownerId, LocalDateTime start);

    @Query("SELECT booking FROM Booking booking WHERE " +
            "booking.item.owner.id = ?1 AND (?2 BETWEEN booking.start AND booking.end) " +
            "ORDER BY booking.start DESC")
    List<Booking> findCurrentByOwnerId(int ownerId, LocalDateTime now);

    List<Booking> findByItemIdOrderByStartDesc(int itemId);

    List<Booking> findByItemIdInOrderByStartDesc(Collection<Integer> itemIds);
}
