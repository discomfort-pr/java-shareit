package ru.practicum.shareit.item.comment.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.InternalValidationException;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void postComment_WithValidData_ShouldSaveAndReturnComment() {
        Long userId = 1L;
        Long itemId = 1L;
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

        User booker = new User();
        booker.setId(userId);

        Item item = new Item();
        item.setId(itemId);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setEnd(pastTime);

        Comment comment = new Comment();
        comment.setText("Great item!");
        comment.setItem(new Item());
        comment.setAuthor(new User());

        Comment savedComment = new Comment();
        savedComment.setId(1L);
        savedComment.setText("Great item!");

        when(bookingRepository.findByItemId(itemId)).thenReturn(Optional.of(booking));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        Comment result = commentService.postComment(userId, itemId, comment);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Great item!", result.getText());
        assertEquals(userId, comment.getAuthor().getId());
        assertEquals(itemId, comment.getItem().getId());
        verify(commentRepository).save(comment);
    }

    @Test
    void postComment_WithNonExistingBooking_ShouldThrowBookingNotFoundException() {
        Long userId = 1L;
        Long itemId = 999L;
        Comment comment = new Comment();

        when(bookingRepository.findByItemId(itemId)).thenReturn(Optional.empty());

        BookingNotFoundException exception = assertThrows(
                BookingNotFoundException.class,
                () -> commentService.postComment(userId, itemId, comment)
        );

        assertEquals("Booking with item 999 not found", exception.getMessage());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void postComment_WithNonApprovedBooking_ShouldThrowInternalValidationException() {
        Long userId = 1L;
        Long itemId = 1L;
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

        User booker = new User();
        booker.setId(userId);

        Booking booking = new Booking();
        booking.setItem(new Item());
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        booking.setEnd(pastTime);

        Comment comment = new Comment();

        when(bookingRepository.findByItemId(itemId)).thenReturn(Optional.of(booking));

        InternalValidationException exception = assertThrows(
                InternalValidationException.class,
                () -> commentService.postComment(userId, itemId, comment)
        );

        assertEquals("Item 1 has not been rented", exception.getMessage());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void postComment_WithRejectedBooking_ShouldThrowInternalValidationException() {
        Long userId = 1L;
        Long itemId = 1L;
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

        User booker = new User();
        booker.setId(userId);

        Booking booking = new Booking();
        booking.setItem(new Item());
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.REJECTED);
        booking.setEnd(pastTime);

        Comment comment = new Comment();

        when(bookingRepository.findByItemId(itemId)).thenReturn(Optional.of(booking));

        InternalValidationException exception = assertThrows(
                InternalValidationException.class,
                () -> commentService.postComment(userId, itemId, comment)
        );

        assertEquals("Item 1 has not been rented", exception.getMessage());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void postComment_WithDifferentUser_ShouldThrowInternalValidationException() {
        Long bookingUserId = 1L;
        Long differentUserId = 2L;
        Long itemId = 1L;
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

        User booker = new User();
        booker.setId(bookingUserId);

        Booking booking = new Booking();
        booking.setItem(new Item());
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setEnd(pastTime);

        Comment comment = new Comment();

        when(bookingRepository.findByItemId(itemId)).thenReturn(Optional.of(booking));

        InternalValidationException exception = assertThrows(
                InternalValidationException.class,
                () -> commentService.postComment(differentUserId, itemId, comment)
        );

        assertEquals("Other users cannot leave comments", exception.getMessage());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void postComment_WithActiveRental_ShouldThrowInternalValidationException() {
        Long userId = 1L;
        Long itemId = 1L;
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);

        User booker = new User();
        booker.setId(userId);

        Booking booking = new Booking();
        booking.setItem(new Item());
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setEnd(futureTime);

        Comment comment = new Comment();

        when(bookingRepository.findByItemId(itemId)).thenReturn(Optional.of(booking));

        InternalValidationException exception = assertThrows(
                InternalValidationException.class,
                () -> commentService.postComment(userId, itemId, comment)
        );

        assertEquals("User did not end the rent", exception.getMessage());
        verify(commentRepository, never()).save(any());
    }
}