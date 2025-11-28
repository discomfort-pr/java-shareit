package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingCategory;

import java.util.List;

public interface BookingService {
    Booking addBooking(Long userId, Booking booking);

    Booking processBooking(Long bookingId, Long userId, String approved);

    Booking getBooking(Long userId, Long bookingId);

    List<Booking> getUserBookings(Long userId, BookingCategory category);

    List<Booking> getUserItemsBookings(Long userId, BookingCategory category);
}
