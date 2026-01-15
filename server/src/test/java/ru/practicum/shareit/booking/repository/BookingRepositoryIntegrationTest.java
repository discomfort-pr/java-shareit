package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(scripts = {"/test-schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class BookingRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker1;
    private User booker2;
    private Item item1;
    private Item item2;
    private Item item3;

    @BeforeEach
    void setUp() {
        owner = createUser("owner@example.com", "Owner");
        booker1 = createUser("booker1@example.com", "Booker One");
        booker2 = createUser("booker2@example.com", "Booker Two");

        entityManager.persist(owner);
        entityManager.persist(booker1);
        entityManager.persist(booker2);

        item1 = createItem("Item 1", "Description 1", owner, true);
        item2 = createItem("Item 2", "Description 2", owner, true);
        item3 = createItem("Item 3", "Description 3", owner, true);

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(item3);

        entityManager.flush();
    }

    @Test
    void findByItemId_WithExistingItem_ShouldReturnBooking() {
        Booking booking = createBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item1,
                booker1,
                BookingStatus.WAITING
        );
        entityManager.persist(booking);
        entityManager.flush();

        Optional<Booking> result = bookingRepository.findByItemId(item1.getId());

        assertTrue(result.isPresent());
        assertEquals(item1.getId(), result.get().getItem().getId());
        assertEquals(booker1.getId(), result.get().getBooker().getId());
    }

    @Test
    void findByItemId_WithNonExistingItem_ShouldReturnEmpty() {
        Optional<Booking> result = bookingRepository.findByItemId(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void findByEndBefore_ShouldReturnPastBookings() {
        LocalDateTime pastTime = LocalDateTime.now().minusDays(5);
        Booking pastBooking = createBooking(
                pastTime.minusDays(2),
                pastTime.minusDays(1),
                item1,
                booker1,
                BookingStatus.APPROVED
        );
        entityManager.persist(pastBooking);
        entityManager.flush();

        List<Booking> result = bookingRepository.findByEndBefore(LocalDateTime.now());

        assertFalse(result.isEmpty());
        assertTrue(result.get(0).getEnd().isBefore(LocalDateTime.now()));
    }

    @Test
    void findByStartAfter_ShouldReturnFutureBookings() {
        Booking futureBooking = createBooking(
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(10),
                item1,
                booker1,
                BookingStatus.APPROVED
        );
        entityManager.persist(futureBooking);
        entityManager.flush();

        List<Booking> result = bookingRepository.findByStartAfter(LocalDateTime.now());

        assertFalse(result.isEmpty());
        assertTrue(result.get(0).getStart().isAfter(LocalDateTime.now()));
    }

    @Test
    void findAllByBookerId_ShouldReturnAllBookingsForBooker() {
        Booking booking1 = createBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item1,
                booker1,
                BookingStatus.WAITING
        );
        Booking booking2 = createBooking(
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4),
                item2,
                booker1,
                BookingStatus.APPROVED
        );
        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.flush();

        List<Booking> result = bookingRepository.findAllByBookerId(booker1.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(booking -> booking.getBooker().getId().equals(booker1.getId())));
        assertTrue(result.get(0).getStart().isAfter(result.get(1).getStart()));
    }

    @Test
    void findCurrentByBookerId_ShouldReturnCurrentBookings() {
        Booking currentBooking = createBooking(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                item1,
                booker1,
                BookingStatus.APPROVED
        );
        entityManager.persist(currentBooking);
        entityManager.flush();

        List<Booking> result = bookingRepository.findCurrentByBookerId(booker1.getId());

        assertEquals(1, result.size());
        assertEquals(currentBooking.getId(), result.get(0).getId());
    }

    @Test
    void findPastByBookerId_ShouldReturnPastBookings() {
        Booking pastBooking = createBooking(
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(3),
                item1,
                booker1,
                BookingStatus.APPROVED
        );
        entityManager.persist(pastBooking);
        entityManager.flush();

        List<Booking> result = bookingRepository.findPastByBookerId(booker1.getId());

        assertEquals(1, result.size());
        assertEquals(pastBooking.getId(), result.get(0).getId());
        assertTrue(result.get(0).getEnd().isBefore(LocalDateTime.now()));
    }

    @Test
    void findFutureByBookerId_ShouldReturnFutureBookings() {
        Booking futureBooking = createBooking(
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(4),
                item1,
                booker1,
                BookingStatus.APPROVED
        );
        entityManager.persist(futureBooking);
        entityManager.flush();

        List<Booking> result = bookingRepository.findFutureByBookerId(booker1.getId());

        assertEquals(1, result.size());
        assertEquals(futureBooking.getId(), result.get(0).getId());
        assertTrue(result.get(0).getStart().isAfter(LocalDateTime.now()));
    }

    @Test
    void findWaitingByBookerId_ShouldReturnWaitingBookings() {
        Booking waitingBooking = createBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item1,
                booker1,
                BookingStatus.WAITING
        );
        Booking approvedBooking = createBooking(
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4),
                item2,
                booker1,
                BookingStatus.APPROVED
        );
        entityManager.persist(waitingBooking);
        entityManager.persist(approvedBooking);
        entityManager.flush();

        List<Booking> result = bookingRepository.findWaitingByBookerId(booker1.getId());

        assertEquals(1, result.size());
        assertEquals(BookingStatus.WAITING, result.get(0).getStatus());
        assertEquals(waitingBooking.getId(), result.get(0).getId());
    }

    @Test
    void findRejectedByBookerId_ShouldReturnRejectedBookings() {
        Booking rejectedBooking = createBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item1,
                booker1,
                BookingStatus.REJECTED
        );
        entityManager.persist(rejectedBooking);
        entityManager.flush();

        List<Booking> result = bookingRepository.findRejectedByBookerId(booker1.getId());

        assertEquals(1, result.size());
        assertEquals(BookingStatus.REJECTED, result.get(0).getStatus());
        assertEquals(rejectedBooking.getId(), result.get(0).getId());
    }

    @Test
    void findAllByItemIdIn_ShouldReturnBookingsForItems() {
        Booking booking1 = createBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item1,
                booker1,
                BookingStatus.WAITING
        );
        Booking booking2 = createBooking(
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4),
                item2,
                booker2,
                BookingStatus.APPROVED
        );
        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.flush();

        List<Booking> result = bookingRepository.findAllByItemIdIn(List.of(item1.getId(), item2.getId()));

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(b -> b.getItem().getId().equals(item1.getId())));
        assertTrue(result.stream().anyMatch(b -> b.getItem().getId().equals(item2.getId())));
        assertTrue(result.get(0).getStart().isAfter(result.get(1).getStart()));
    }

    @Test
    void findCurrentByItemIdIn_ShouldReturnCurrentBookingsForItems() {
        Booking currentBooking = createBooking(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                item1,
                booker1,
                BookingStatus.APPROVED
        );
        entityManager.persist(currentBooking);
        entityManager.flush();

        List<Booking> result = bookingRepository.findCurrentByItemIdIn(List.of(item1.getId()));

        assertEquals(1, result.size());
        assertEquals(currentBooking.getId(), result.get(0).getId());
    }

    @Test
    void findPastByItemIdIn_ShouldReturnPastBookingsForItems() {
        Booking pastBooking = createBooking(
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(3),
                item1,
                booker1,
                BookingStatus.APPROVED
        );
        entityManager.persist(pastBooking);
        entityManager.flush();

        List<Booking> result = bookingRepository.findPastByItemIdIn(List.of(item1.getId()));

        assertEquals(1, result.size());
        assertEquals(pastBooking.getId(), result.get(0).getId());
    }

    @Test
    void findFutureByItemIdIn_ShouldReturnFutureBookingsForItems() {
        Booking futureBooking = createBooking(
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(4),
                item1,
                booker1,
                BookingStatus.APPROVED
        );
        entityManager.persist(futureBooking);
        entityManager.flush();

        List<Booking> result = bookingRepository.findFutureByItemIdIn(List.of(item1.getId()));

        assertEquals(1, result.size());
        assertEquals(futureBooking.getId(), result.get(0).getId());
    }

    @Test
    void findWaitingByItemIdIn_ShouldReturnWaitingBookingsForItems() {
        Booking waitingBooking = createBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item1,
                booker1,
                BookingStatus.WAITING
        );
        entityManager.persist(waitingBooking);
        entityManager.flush();

        List<Booking> result = bookingRepository.findWaitingByItemIdIn(List.of(item1.getId()));

        assertEquals(1, result.size());
        assertEquals(BookingStatus.WAITING, result.get(0).getStatus());
        assertEquals(waitingBooking.getId(), result.get(0).getId());
    }

    @Test
    void findRejectedByItemIdIn_ShouldReturnRejectedBookingsForItems() {
        Booking rejectedBooking = createBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item1,
                booker1,
                BookingStatus.REJECTED
        );
        entityManager.persist(rejectedBooking);
        entityManager.flush();

        List<Booking> result = bookingRepository.findRejectedByItemIdIn(List.of(item1.getId()));

        assertEquals(1, result.size());
        assertEquals(BookingStatus.REJECTED, result.get(0).getStatus());
        assertEquals(rejectedBooking.getId(), result.get(0).getId());
    }

    @Test
    void findAllByItemIdIn_WithMultipleItems_ShouldReturnCorrectBookings() {
        Booking booking1 = createBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item1,
                booker1,
                BookingStatus.WAITING
        );
        Booking booking2 = createBooking(
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4),
                item2,
                booker2,
                BookingStatus.APPROVED
        );
        Booking booking3 = createBooking(
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(6),
                item3,
                booker1,
                BookingStatus.REJECTED
        );
        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.persist(booking3);
        entityManager.flush();

        List<Booking> result = bookingRepository.findAllByItemIdIn(List.of(item1.getId(), item2.getId()));

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(b -> b.getItem().getId().equals(item1.getId())));
        assertTrue(result.stream().anyMatch(b -> b.getItem().getId().equals(item2.getId())));
        assertTrue(result.stream().noneMatch(b -> b.getItem().getId().equals(item3.getId())));
    }

    @Test
    void findAllByItemIdIn_WithEmptyList_ShouldReturnEmptyList() {
        List<Booking> result = bookingRepository.findAllByItemIdIn(List.of());

        assertTrue(result.isEmpty());
    }

    @Test
    void save_ShouldPersistBookingWithCorrectRelations() {
        Booking newBooking = createBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item1,
                booker1,
                BookingStatus.WAITING
        );

        Booking savedBooking = bookingRepository.save(newBooking);

        assertNotNull(savedBooking.getId());
        assertEquals(item1.getId(), savedBooking.getItem().getId());
        assertEquals(booker1.getId(), savedBooking.getBooker().getId());
        assertEquals(BookingStatus.WAITING, savedBooking.getStatus());

        Optional<Booking> retrieved = bookingRepository.findById(savedBooking.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(savedBooking.getId(), retrieved.get().getId());
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

    private Booking createBooking(LocalDateTime start, LocalDateTime end, Item item, User booker, BookingStatus status) {
        Booking booking = new Booking();
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(status);
        return booking;
    }
}