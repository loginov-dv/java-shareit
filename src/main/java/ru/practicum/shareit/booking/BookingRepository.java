package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;

import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(int bookedId, String status);

    List<Booking> findByBookerIdOrderByStartDesc(int bookerId);

    @Query("SELECT booking FROM Booking booking WHERE " +
            "booking.booker.id = ?1 AND booking.end < LOCALTIMESTAMP " +
            "ORDER BY booking.start DESC")
    List<Booking> findPastByBookerId(int bookerId);

    @Query("SELECT booking FROM Booking booking WHERE " +
            "booking.booker.id = ?1 AND booking.start > LOCALTIMESTAMP " +
            "ORDER BY booking.start DESC")
    List<Booking> findFutureByBookerId(int bookerId);

    @Query("SELECT booking FROM Booking booking WHERE " +
            "booking.booker.id = ?1 AND (LOCALTIMESTAMP BETWEEN booking.start AND booking.end) " +
            "ORDER BY booking.start DESC")
    List<Booking> findCurrentByBookedId(int bookerId);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(int ownerId, String status);

    List<Booking> findByItemOwnerIdOrderByStartDesc(int ownerId);

    @Query("SELECT booking FROM Booking booking WHERE " +
            "booking.item.owner.id = ?1 AND booking.end < LOCALTIMESTAMP " +
            "ORDER BY booking.start DESC")
    List<Booking> findPastByOwnerId(int ownerId);

    @Query("SELECT booking FROM Booking booking WHERE " +
            "booking.item.owner.id = ?1 AND booking.start > LOCALTIMESTAMP " +
            "ORDER BY booking.start DESC")
    List<Booking> findFutureByOwnerId(int ownerId);

    @Query("SELECT booking FROM Booking booking WHERE " +
            "booking.item.owner.id = ?1 AND (LOCALTIMESTAMP BETWEEN booking.start AND booking.end) " +
            "ORDER BY booking.start DESC")
    List<Booking> findCurrentByOwnerId(int ownerId);

    List<Booking> findByItemIdOrderByStartDesc(int itemId);

    List<Booking> findByItemIdInOrderByStartDesc(Collection<Integer> itemIds);
}
