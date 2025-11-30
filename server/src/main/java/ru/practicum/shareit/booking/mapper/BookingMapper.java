package ru.practicum.shareit.booking.mapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ShortItemDto;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.ShortUserDto;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingMapper {

    UserRepository userRepository;
    ItemRepository itemRepository;

    public Booking toEntity(BookingDtoIn bookingData, Long userId) {
        return new Booking(
                null,
                bookingData.getStart(),
                bookingData.getEnd(),
                itemRepository.findById(bookingData.getItemId())
                        .orElseThrow(() -> new ItemNotFoundException(
                                String.format("Item with id %d not found", bookingData.getItemId())
                        )),
                userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException(
                                String.format("User with id %d not found", userId)
                        )),
                BookingStatus.WAITING
        );
    }

    public BookingDtoOut toBookingDtoOut(Booking booking) {
        return new BookingDtoOut(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                new ShortItemDto(booking.getItem().getId(), booking.getItem().getName()),
                new ShortUserDto(booking.getBooker().getId(), booking.getBooker().getName()),
                booking.getStatus()
        );
    }

    public List<BookingDtoOut> toBookingDtoOutList(List<Booking> bookings) {
        return bookings.stream()
                .map(this::toBookingDtoOut)
                .toList();
    }
}
