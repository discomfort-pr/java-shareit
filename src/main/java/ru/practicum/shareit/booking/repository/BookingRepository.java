package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByItemId(Long itemId);

    List<Booking> findByEndBefore(LocalDateTime dateTime);

    List<Booking> findByStartAfter(LocalDateTime dateTime);


    @Query(
            "SELECT b FROM Booking as b " +
            "WHERE booker.id = ?1 " +
            "ORDER BY start DESC"
    )
    List<Booking> findAllByBookerId(Long bookerId);

    @Query("SELECT b FROM Booking b " +
            "WHERE booker.id = ?1 " +
            "AND start < CURRENT_TIMESTAMP " +
            "AND end > CURRENT_TIMESTAMP " +
            "ORDER BY start DESC")
    List<Booking> findCurrentByBookerId(Long bookerId);

    @Query("SELECT b FROM Booking b " +
            "WHERE booker.id = ?1 " +
            "AND end < CURRENT_TIMESTAMP " +
            "ORDER BY start DESC")
    List<Booking> findPastByBookerId(Long bookerId);

    @Query("SELECT b FROM Booking b " +
            "WHERE booker.id = ?1 " +
            "AND start > CURRENT_TIMESTAMP " +
            "ORDER BY start DESC")
    List<Booking> findFutureByBookerId(Long bookerId);

    @Query("SELECT b FROM Booking b " +
            "WHERE booker.id = ?1 " +
            "AND status = 'WAITING'" +
            "ORDER BY start DESC")
    List<Booking> findWaitingByBookerId(Long bookerId);

    @Query("SELECT b FROM Booking b " +
            "WHERE booker.id = ?1 " +
            "AND status = 'REJECTED'" +
            "ORDER BY start DESC")
    List<Booking> findRejectedByBookerId(Long bookerId);

    @Query(
            "SELECT b FROM Booking as b " +
                    "WHERE item.id IN ?1 " +
                    "ORDER BY start DESC"
    )
    List<Booking> findAllByItemIdIn(List<Long> items);

    @Query("SELECT b FROM Booking b " +
            "WHERE item.id IN ?1 " +
            "AND start < CURRENT_TIMESTAMP " +
            "AND end > CURRENT_TIMESTAMP " +
            "ORDER BY start DESC")
    List<Booking> findCurrentByItemIdIn(List<Long> items);

    @Query("SELECT b FROM Booking b " +
            "WHERE item.id IN ?1 " +
            "AND end < CURRENT_TIMESTAMP " +
            "ORDER BY start DESC")
    List<Booking> findPastByItemIdIn(List<Long> items);

    @Query("SELECT b FROM Booking b " +
            "WHERE item.id IN ?1 " +
            "AND start > CURRENT_TIMESTAMP " +
            "ORDER BY start DESC")
    List<Booking> findFutureByItemIdIn(List<Long> items);

    @Query("SELECT b FROM Booking b " +
            "WHERE item.id IN ?1 " +
            "AND status = 'WAITING'" +
            "ORDER BY start DESC")
    List<Booking> findWaitingByItemIdIn(List<Long> items);

    @Query("SELECT b FROM Booking b " +
            "WHERE item.id IN ?1 " +
            "AND status = 'REJECTED'" +
            "ORDER BY start DESC")
    List<Booking> findRejectedByItemIdIn(List<Long> items);
}
