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
        Item item = itemRepository.findById(booking.getItem().getId())
                .orElseThrow(() -> new ItemNotFoundException(
                        String.format("Item with id %d not found", booking.getItem().getId())
                ));

        if (!item.getAvailable()) {
            throw new InternalValidationException("Cannot book unavailable item");
        }
        if (booking.getStart().equals(booking.getEnd())) {
            throw new InternalValidationException("Start time cannot be equal to end time");
        }
        booking.getBooker().setId(userId);

        return bookingRepository.save(booking);
    }

    @Override
    public Booking processBooking(Long bookingId, Long userId, String approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(
                        String.format("Booking with id %d not found", bookingId)
                ));
        Item item = itemRepository.findById(booking.getItem().getId())
                .orElseThrow(() -> new ItemNotFoundException(
                        String.format("Item with id %d not found", booking.getItem().getId())
                ));

        if (!Objects.equals(item.getOwner().getId(), userId)) {
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
        Item item = itemRepository.findById(booking.getItem().getId())
                .orElseThrow(() -> new ItemNotFoundException(
                        String.format("Item with id %d not found", booking.getItem().getId())
                ));

        if (!Objects.equals(userId, item.getOwner().getId()) && !Objects.equals(userId, booking.getBooker().getId())) {
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
            case ALL -> bookingRepository.findAllByBookerId(userId);
            case CURRENT -> bookingRepository.findCurrentByBookerId(userId);
            case PAST -> bookingRepository.findPastByBookerId(userId);
            case FUTURE -> bookingRepository.findFutureByBookerId(userId);
            case WAITING -> bookingRepository.findWaitingByBookerId(userId);
            case REJECTED -> bookingRepository.findRejectedByBookerId(userId);
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
            case ALL -> bookingRepository.findAllByItemIdIn(itemsId);
            case CURRENT -> bookingRepository.findCurrentByItemIdIn(itemsId);
            case PAST -> bookingRepository.findPastByItemIdIn(itemsId);
            case FUTURE -> bookingRepository.findFutureByItemIdIn(itemsId);
            case WAITING -> bookingRepository.findWaitingByItemIdIn(itemsId);
            case REJECTED -> bookingRepository.findRejectedByItemIdIn(itemsId);
        };
    }
}
