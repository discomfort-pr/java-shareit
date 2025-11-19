package ru.practicum.shareit.booking.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingCategory;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.InternalValidationException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Transactional
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingServiceImpl implements BookingService {

    BookingRepository bookingRepository;
    ItemRepository itemRepository;
    UserRepository userRepository;

    @Override
    public Booking addBooking(Long userId, Booking booking) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User with id %d not found", userId)
                ));
        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new ItemNotFoundException(
                        String.format("Item with id %d not found", booking.getItemId())
                ));

        if (!item.getAvailable()) {
            throw new InternalValidationException("Cannot book unavailable item");
        }
        if (booking.getStart().equals(booking.getEnd())) {
            throw new InternalValidationException("Start time cannot be equal to end time");
        }
        booking.setBookerId(userId);

        return bookingRepository.save(booking);
    }

    @Override
    public Booking processBooking(Long bookingId, Long userId, String approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(
                        String.format("Booking with id %d not found", bookingId)
                ));
        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new ItemNotFoundException(
                        String.format("Item with id %d not found", booking.getItemId())
                ));

        if (!Objects.equals(item.getOwnerId(), userId)) {
            throw new InternalValidationException("Item requests can be approved by item owner");
        }

        boolean isApproved;
        if (approved.equals("true")) {
            isApproved = true;
        } else if (approved.equals("false")) {
            isApproved = false;
        } else {
            throw new InternalValidationException("Invalid 'approved' parameter value (can be true or false)");
        }

        booking.setStatus(isApproved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return bookingRepository.save(booking);
    }

    @Override
    public Booking getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(
                        String.format("Booking with id %d not found", bookingId)
                ));
        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new ItemNotFoundException(
                        String.format("Item with id %d not found", booking.getItemId())
                ));

        if (!Objects.equals(userId, item.getOwnerId()) && !Objects.equals(userId, booking.getBookerId())) {
            throw new InternalValidationException("You must be a booker or item owner to get booking info");
        }

        return booking;
    }

    @Override
    public List<Booking> getUserBookings(Long userId, BookingCategory category) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User with id %d not found", userId)
                ));

        return switch (category) {
            case ALL -> bookingRepository.findBookingByBookerId(userId).stream()
                    .sorted(Comparator.comparing(Booking::getStart))
                    .toList().reversed();
            case CURRENT -> bookingRepository.findBookingByBookerId(userId).stream()
                    .filter(
                            booking -> booking.getStart().isBefore(LocalDateTime.now()) &&
                                    booking.getEnd().isAfter(LocalDateTime.now())
                    )
                    .sorted(Comparator.comparing(Booking::getStart))
                    .toList().reversed();
            case PAST -> bookingRepository.findBookingByBookerId(userId).stream()
                    .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                    .sorted(Comparator.comparing(Booking::getStart))
                    .toList().reversed();
            case FUTURE -> bookingRepository.findBookingByBookerId(userId).stream()
                    .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                    .sorted(Comparator.comparing(Booking::getStart))
                    .toList().reversed();
            case WAITING -> bookingRepository.findBookingByBookerId(userId).stream()
                    .filter(booking -> booking.getStatus().equals(BookingStatus.WAITING))
                    .sorted(Comparator.comparing(Booking::getStart))
                    .toList().reversed();
            case REJECTED -> bookingRepository.findBookingByBookerId(userId).stream()
                    .filter(booking -> booking.getStatus().equals(BookingStatus.REJECTED))
                    .sorted(Comparator.comparing(Booking::getStart))
                    .toList().reversed();
        };
    }

    @Override
    public List<Booking> getUserItemsBookings(Long userId, BookingCategory category) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User with id %d not found", userId)
                ));

        List<Long> itemsId = itemRepository.findByOwnerId(userId).stream()
                .map(Item::getId)
                .toList();

        return switch (category) {
            case ALL -> bookingRepository.findBookingByItemIdIn(itemsId).stream()
                    .sorted(Comparator.comparing(Booking::getStart))
                    .toList().reversed();
            case CURRENT -> bookingRepository.findBookingByItemIdIn(itemsId).stream()
                    .filter(
                            booking -> booking.getStart().isBefore(LocalDateTime.now()) &&
                                    booking.getEnd().isAfter(LocalDateTime.now())
                    )
                    .sorted(Comparator.comparing(Booking::getStart))
                    .toList().reversed();
            case PAST -> bookingRepository.findBookingByItemIdIn(itemsId).stream()
                    .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                    .sorted(Comparator.comparing(Booking::getStart))
                    .toList().reversed();
            case FUTURE -> bookingRepository.findBookingByItemIdIn(itemsId).stream()
                    .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                    .sorted(Comparator.comparing(Booking::getStart))
                    .toList().reversed();
            case WAITING -> bookingRepository.findBookingByItemIdIn(itemsId).stream()
                    .filter(booking -> booking.getStatus().equals(BookingStatus.WAITING))
                    .sorted(Comparator.comparing(Booking::getStart))
                    .toList().reversed();
            case REJECTED -> bookingRepository.findBookingByItemIdIn(itemsId).stream()
                    .filter(booking -> booking.getStatus().equals(BookingStatus.REJECTED))
                    .sorted(Comparator.comparing(Booking::getStart))
                    .toList().reversed();
        };
    }
}
