package ru.practicum.shareit.item.comment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
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

@SpringBootTest
@Transactional
@Sql(scripts = {"/test-schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CommentServiceIntegrationTest {

    @Autowired
    private CommentServiceImpl commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ru.practicum.shareit.item.repository.ItemRepository itemRepository;

    @Autowired
    private ru.practicum.shareit.user.repository.UserRepository userRepository;

    private User owner;
    private User booker;
    private User anotherUser;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = createUser("owner@example.com", "Owner");
        booker = createUser("booker@example.com", "Booker");
        anotherUser = createUser("another@example.com", "Another User");

        userRepository.save(owner);
        userRepository.save(booker);
        userRepository.save(anotherUser);

        item = createItem("Test Item", "Test Description", owner, true);
        itemRepository.save(item);
    }

    @Test
    void postComment_WithValidCompletedBooking_ShouldSaveComment() {
        Booking booking = createBooking(item, booker, BookingStatus.APPROVED,
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
        bookingRepository.save(booking);

        Comment comment = new Comment();
        comment.setText("Excellent item, worked perfectly!");
        comment.setItem(new Item());
        comment.setAuthor(new User());

        Comment result = commentService.postComment(booker.getId(), item.getId(), comment);

        assertNotNull(result.getId());
        assertEquals("Excellent item, worked perfectly!", result.getText());
        assertEquals(booker.getId(), comment.getAuthor().getId());
        assertEquals(item.getId(), comment.getItem().getId());

        Optional<Comment> fromDb = commentRepository.findById(result.getId());
        assertTrue(fromDb.isPresent());
        assertEquals(result.getId(), fromDb.get().getId());
        assertEquals("Excellent item, worked perfectly!", fromDb.get().getText());
    }

    @Test
    void postComment_WithNonExistingBooking_ShouldThrowBookingNotFoundException() {
        Comment comment = new Comment();
        comment.setText("Test comment");
        comment.setItem(new Item());
        comment.setAuthor(new User());

        BookingNotFoundException exception = assertThrows(
                BookingNotFoundException.class,
                () -> commentService.postComment(booker.getId(), 999L, comment)
        );

        assertEquals("Booking with item 999 not found", exception.getMessage());
    }

    @Test
    void postComment_WithWaitingBooking_ShouldThrowInternalValidationException() {
        Booking booking = createBooking(item, booker, BookingStatus.WAITING,
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
        bookingRepository.save(booking);

        Comment comment = new Comment();
        comment.setText("Test comment");
        comment.setItem(new Item());
        comment.setAuthor(new User());

        InternalValidationException exception = assertThrows(
                InternalValidationException.class,
                () -> commentService.postComment(booker.getId(), item.getId(), comment)
        );

        assertEquals("Item " + item.getId() + " has not been rented", exception.getMessage());
    }

    @Test
    void postComment_WithRejectedBooking_ShouldThrowInternalValidationException() {
        Booking booking = createBooking(item, booker, BookingStatus.REJECTED,
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
        bookingRepository.save(booking);

        Comment comment = new Comment();
        comment.setText("Test comment");
        comment.setItem(new Item());
        comment.setAuthor(new User());

        InternalValidationException exception = assertThrows(
                InternalValidationException.class,
                () -> commentService.postComment(booker.getId(), item.getId(), comment)
        );

        assertEquals("Item " + item.getId() + " has not been rented", exception.getMessage());
    }

    @Test
    void postComment_WithDifferentUser_ShouldThrowInternalValidationException() {
        Booking booking = createBooking(item, booker, BookingStatus.APPROVED,
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
        bookingRepository.save(booking);

        Comment comment = new Comment();
        comment.setText("Test comment");
        comment.setItem(new Item());
        comment.setAuthor(new User());

        InternalValidationException exception = assertThrows(
                InternalValidationException.class,
                () -> commentService.postComment(anotherUser.getId(), item.getId(), comment)
        );

        assertEquals("Other users cannot leave comments", exception.getMessage());
    }

    @Test
    void postComment_WithActiveRental_ShouldThrowInternalValidationException() {
        Booking booking = createBooking(item, booker, BookingStatus.APPROVED,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        bookingRepository.save(booking);

        Comment comment = new Comment();
        comment.setText("Test comment");
        comment.setItem(new Item());
        comment.setAuthor(new User());

        InternalValidationException exception = assertThrows(
                InternalValidationException.class,
                () -> commentService.postComment(booker.getId(), item.getId(), comment)
        );

        assertEquals("User did not end the rent", exception.getMessage());
    }

    @Test
    void postComment_WithMultipleCommentsForSameItem_ShouldSaveAllComments() {
        Booking booking = createBooking(item, booker, BookingStatus.APPROVED,
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1));
        bookingRepository.save(booking);

        Comment comment1 = new Comment();
        comment1.setText("First comment");
        comment1.setItem(new Item());
        comment1.setAuthor(new User());

        Comment comment2 = new Comment();
        comment2.setText("Second comment");
        comment2.setItem(new Item());
        comment2.setAuthor(new User());

        Comment result1 = commentService.postComment(booker.getId(), item.getId(), comment1);
        Comment result2 = commentService.postComment(booker.getId(), item.getId(), comment2);

        assertNotNull(result1.getId());
        assertNotNull(result2.getId());
        assertEquals("First comment", result1.getText());
        assertEquals("Second comment", result2.getText());

        long commentCount = commentRepository.findByItemId(item.getId()).size();
        assertEquals(2, commentCount);
    }

    @Test
    void postComment_ShouldSetCorrectItemAndAuthorIds() {
        Booking booking = createBooking(item, booker, BookingStatus.APPROVED,
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
        bookingRepository.save(booking);

        Comment comment = new Comment();
        comment.setText("Test comment with IDs");
        comment.setItem(new Item());
        comment.setAuthor(new User());

        Comment result = commentService.postComment(booker.getId(), item.getId(), comment);

        assertEquals(booker.getId(), comment.getAuthor().getId());
        assertEquals(item.getId(), comment.getItem().getId());
        assertEquals(booker.getId(), result.getAuthor().getId());
        assertEquals(item.getId(), result.getItem().getId());
    }

    private User createUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }

    private Item createItem(String name, String description, User owner, Boolean available) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(owner);
        return item;
    }

    private Booking createBooking(Item item, User booker, BookingStatus status, LocalDateTime start, LocalDateTime end) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(status);
        booking.setStart(start);
        booking.setEnd(end);
        return booking;
    }
}