package ru.practicum.shareit.booking.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.BookingCategory;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingController {

    BookingService bookingService;
    BookingMapper bookingMapper;

    @PostMapping
    public BookingDtoOut addBooking(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody BookingDtoIn bookingData) {
        return bookingMapper.toBookingDtoOut(bookingService.addBooking(userId, bookingMapper.toEntity(bookingData, userId)));
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoOut processBooking(@PathVariable Long bookingId,
                                       @RequestHeader("X-Sharer-User-Id") Long userId,
                                       @RequestParam(name = "approved") String approved) {
        return bookingMapper.toBookingDtoOut(bookingService.processBooking(bookingId, userId, approved));
    }

    @GetMapping("/{bookingId}")
    public BookingDtoOut getBooking(@PathVariable Long bookingId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingMapper.toBookingDtoOut(bookingService.getBooking(userId, bookingId));
    }

    @GetMapping
    public List<BookingDtoOut> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @RequestParam(name = "category", defaultValue = "ALL") String category) {
        return bookingMapper.toBookingDtoOut(bookingService.getUserBookings(userId, BookingCategory.valueOf(category)));
    }

    @GetMapping("/owner")
    public List<BookingDtoOut> getUserItemsBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @RequestParam(name = "category", defaultValue = "ALL") String category) {
        return bookingMapper.toBookingDtoOut(bookingService.getUserItemsBookings(userId, BookingCategory.valueOf(category)));
    }
}
