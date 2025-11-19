package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findBookingByBookerId(Long bookerId);

    Optional<Booking> findByItemId(Long itemId);

    List<Booking> findBookingByItemIdIn(List<Long> itemsId);

    List<Booking> findByEndBefore(LocalDateTime dateTime);

    List<Booking> findByStartAfter(LocalDateTime dateTime);
}
