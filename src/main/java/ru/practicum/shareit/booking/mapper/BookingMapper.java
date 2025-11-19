package ru.practicum.shareit.booking.mapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingMapper {

    UserRepository userRepository;
    ItemRepository itemRepository;

    public BookingDtoIn toBookingDtoIn(Booking booking) {
        return new BookingDtoIn(
                booking.getItemId(),
                booking.getStart(),
                booking.getEnd()
        );
    }

    public Booking toEntity(BookingDtoIn bookingData) {
        return new Booking(
                null,
                bookingData.getStart(),
                bookingData.getEnd(),
                bookingData.getItemId(),
                null,
                BookingStatus.WAITING
        );
    }

    public BookingDtoOut toBookingDtoOut(Booking booking) {
        return new BookingDtoOut(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                itemRepository.findById(booking.getItemId()).orElse(null),
                userRepository.findById(booking.getBookerId()).orElse(null),
                booking.getStatus()
        );
    }

    public List<BookingDtoOut> toBookingDtoOut(List<Booking> bookings) {
        return bookings.stream()
                .map(this::toBookingDtoOut)
                .toList();
    }
}
