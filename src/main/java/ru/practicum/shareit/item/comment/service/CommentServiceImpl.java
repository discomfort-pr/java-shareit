package ru.practicum.shareit.item.comment.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.InternalValidationException;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentServiceImpl implements CommentService {

    CommentRepository commentRepository;
    BookingRepository bookingRepository;

    @Override
    public Comment postComment(Long userId, Long itemId, Comment comment) {
        Booking booking = bookingRepository.findByItemId(itemId)
                .orElseThrow(() -> new BookingNotFoundException(
                        String.format("Booking with item %d not found", itemId)
                ));

        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new InternalValidationException(String.format("Item %d has not been rented", itemId));
        }
        if (!Objects.equals(booking.getBookerId(), userId)) {
            throw new InternalValidationException("Other users cannot leave comments");
        }
        if (!booking.getEnd().isBefore(LocalDateTime.now())) {
            throw new InternalValidationException("User did not end the rent");
        }

        comment.setAuthorId(userId);
        comment.setItemId(itemId);

        return commentRepository.save(comment);
    }
}
