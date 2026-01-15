package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingController {

    BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody BookingDto bookingData) {
        return bookingClient.post(userId, bookingData);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> processBooking(@PathVariable Long bookingId,
                                        @RequestHeader("X-Sharer-User-Id") Long userId,
                                        @RequestParam(name = "approved") String approved) {
        return bookingClient.patch(bookingId, userId, Boolean.valueOf(approved));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@PathVariable Long bookingId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingClient.get(bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @RequestParam(name = "category", defaultValue = "ALL") String category) {
        return bookingClient.get(userId, category, false);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getUserItemsBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @RequestParam(name = "category", defaultValue = "ALL") String category) {
        return bookingClient.get(userId, category, true);
    }
}