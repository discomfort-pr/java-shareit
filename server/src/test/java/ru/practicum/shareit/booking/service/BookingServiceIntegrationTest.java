package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingCategory;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Sql(scripts = {"/test-schema.sql"})
class BookingServiceIntegrationTest {

    @Autowired
    private BookingServiceImpl bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void addBooking_IntegrationTest_ShouldSaveBookingToDatabase() {
        User booker = userRepository.save(createUser("booker@example.com", "Booker"));
        User owner = userRepository.save(createUser("owner@example.com", "Owner"));

        Item item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        Item savedItem = itemRepository.save(item);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(savedItem);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking result = bookingService.addBooking(booker.getId(), booking);

        assertNotNull(result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
        assertEquals(savedItem.getId(), result.getItem().getId());
        assertEquals(booker.getId(), result.getBooker().getId());
    }

    @Test
    void processBooking_IntegrationTest_ShouldUpdateBookingStatus() {
        User booker = userRepository.save(createUser("booker2@example.com", "Booker2"));
        User owner = userRepository.save(createUser("owner2@example.com", "Owner2"));

        Item item = new Item();
        item.setName("Test Item 2");
        item.setDescription("Test Description 2");
        item.setAvailable(true);
        item.setOwner(owner);
        Item savedItem = itemRepository.save(item);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(savedItem);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        Booking savedBooking = bookingService.addBooking(booker.getId(), booking);

        Booking result = bookingService.processBooking(savedBooking.getId(), owner.getId(), "true");

        assertEquals(BookingStatus.APPROVED, result.getStatus());
        assertEquals(savedBooking.getId(), result.getId());
    }

    @Test
    void getUserBookings_IntegrationTest_ShouldReturnUserBookings() {
        User booker = userRepository.save(createUser("booker3@example.com", "Booker3"));
        User owner = userRepository.save(createUser("owner3@example.com", "Owner3"));

        Item item = new Item();
        item.setName("Test Item 3");
        item.setDescription("Test Description 3");
        item.setAvailable(true);
        item.setOwner(owner);
        Item savedItem = itemRepository.save(item);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(savedItem);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        bookingService.addBooking(booker.getId(), booking);

        List<Booking> result = bookingService.getUserBookings(booker.getId(), BookingCategory.ALL);

        assertFalse(result.isEmpty());
        assertEquals(booker.getId(), result.get(0).getBooker().getId());
    }

    @Test
    void getUserItemsBookings_IntegrationTest_ShouldReturnOwnerItemsBookings() {
        User booker = userRepository.save(createUser("booker4@example.com", "Booker4"));
        User owner = userRepository.save(createUser("owner4@example.com", "Owner4"));

        Item item = new Item();
        item.setName("Test Item 4");
        item.setDescription("Test Description 4");
        item.setAvailable(true);
        item.setOwner(owner);
        Item savedItem = itemRepository.save(item);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(savedItem);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        bookingService.addBooking(booker.getId(), booking);

        List<Booking> result = bookingService.getUserItemsBookings(owner.getId(), BookingCategory.ALL);

        assertFalse(result.isEmpty());
        assertEquals(savedItem.getId(), result.get(0).getItem().getId());
    }

    @Test
    void addBooking_WithNonExistentUser_ShouldThrowException() {
        User owner = userRepository.save(createUser("owner5@example.com", "Owner5"));

        Item item = new Item();
        item.setName("Test Item 5");
        item.setDescription("Test Description 5");
        item.setAvailable(true);
        item.setOwner(owner);
        Item savedItem = itemRepository.save(item);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(savedItem);
        booking.setBooker(owner);

        assertThrows(UserNotFoundException.class,
                () -> bookingService.addBooking(999L, booking));
    }

    private User createUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }
}