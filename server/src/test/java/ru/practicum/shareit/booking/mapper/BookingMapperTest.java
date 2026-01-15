package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingMapperTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingMapper bookingMapper;

    @Test
    void toEntity_WithValidData_ShouldReturnBooking() {
        Long userId = 1L;
        Long itemId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingDtoIn bookingDtoIn = new BookingDtoIn(itemId, start, end);

        User user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setEmail("test@example.com");

        Item item = new Item();
        item.setId(itemId);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        Booking result = bookingMapper.toEntity(bookingDtoIn, userId);

        assertNull(result.getId());
        assertEquals(start, result.getStart());
        assertEquals(end, result.getEnd());
        assertEquals(item, result.getItem());
        assertEquals(user, result.getBooker());
        assertEquals(BookingStatus.WAITING, result.getStatus());
    }

    @Test
    void toEntity_WithNonExistentItem_ShouldThrowItemNotFoundException() {
        Long userId = 1L;
        Long itemId = 999L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingDtoIn bookingDtoIn = new BookingDtoIn(itemId, start, end);

        User user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setEmail("test@example.com");

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> bookingMapper.toEntity(bookingDtoIn, userId)
        );

        assertEquals("Item with id 999 not found", exception.getMessage());
    }

    @Test
    void toEntity_WithNonExistentUser_ShouldThrowUserNotFoundException() {
        Long userId = 999L;
        Long itemId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingDtoIn bookingDtoIn = new BookingDtoIn(itemId, start, end);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(new Item(
                itemId, "", "", true, null, null
        )));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> bookingMapper.toEntity(bookingDtoIn, userId)
        );

        assertEquals("User with id 999 not found", exception.getMessage());
    }

    @Test
    void toBookingDtoOut_WithValidBooking_ShouldReturnBookingDtoOut() {
        Long bookingId = 1L;
        Long userId = 1L;
        Long itemId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        User user = new User();
        user.setId(userId);
        user.setName("Test User");

        Item item = new Item();
        item.setId(itemId);
        item.setName("Test Item");

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.WAITING);

        BookingDtoOut result = bookingMapper.toBookingDtoOut(booking);

        assertEquals(bookingId, result.getId());
        assertEquals(start, result.getStart());
        assertEquals(end, result.getEnd());
        assertEquals(BookingStatus.WAITING, result.getStatus());

        assertNotNull(result.getItem());
        assertEquals(itemId, result.getItem().getId());
        assertEquals("Test Item", result.getItem().getName());

        assertNotNull(result.getBooker());
        assertEquals(userId, result.getBooker().getId());
        assertEquals("Test User", result.getBooker().getName());
    }

    @Test
    void toBookingDtoOut_WithApprovedStatus_ShouldReturnCorrectStatus() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        booking.setBooker(user);

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        booking.setItem(item);

        booking.setStatus(BookingStatus.APPROVED);

        BookingDtoOut result = bookingMapper.toBookingDtoOut(booking);

        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    void toBookingDtoOut_WithRejectedStatus_ShouldReturnCorrectStatus() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        booking.setBooker(user);

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        booking.setItem(item);

        booking.setStatus(BookingStatus.REJECTED);

        BookingDtoOut result = bookingMapper.toBookingDtoOut(booking);

        assertEquals(BookingStatus.REJECTED, result.getStatus());
    }

    @Test
    void toBookingDtoOut_WithCanceledStatus_ShouldReturnCorrectStatus() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        booking.setBooker(user);

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        booking.setItem(item);

        booking.setStatus(BookingStatus.CANCELED);

        BookingDtoOut result = bookingMapper.toBookingDtoOut(booking);

        assertEquals(BookingStatus.CANCELED, result.getStatus());
    }

    @Test
    void toBookingDtoOut_WithNullBooking_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> bookingMapper.toBookingDtoOut(null));
    }

    @Test
    void toBookingDtoOut_List_WithValidBookings_ShouldReturnListOfDto() {
        Long bookingId1 = 1L;
        Long bookingId2 = 2L;

        User user = new User();
        user.setId(1L);
        user.setName("Test User");

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");

        Booking booking1 = new Booking();
        booking1.setId(bookingId1);
        booking1.setStart(LocalDateTime.now().plusDays(1));
        booking1.setEnd(LocalDateTime.now().plusDays(2));
        booking1.setItem(item);
        booking1.setBooker(user);
        booking1.setStatus(BookingStatus.WAITING);

        Booking booking2 = new Booking();
        booking2.setId(bookingId2);
        booking2.setStart(LocalDateTime.now().plusDays(3));
        booking2.setEnd(LocalDateTime.now().plusDays(4));
        booking2.setItem(item);
        booking2.setBooker(user);
        booking2.setStatus(BookingStatus.APPROVED);

        List<Booking> bookings = List.of(booking1, booking2);

        List<BookingDtoOut> result = bookingMapper.toBookingDtoOutList(bookings);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(bookingId1, result.get(0).getId());
        assertEquals(BookingStatus.WAITING, result.get(0).getStatus());

        assertEquals(bookingId2, result.get(1).getId());
        assertEquals(BookingStatus.APPROVED, result.get(1).getStatus());
    }

    @Test
    void toBookingDtoOut_List_WithEmptyList_ShouldReturnEmptyList() {
        List<Booking> emptyBookings = List.of();

        List<BookingDtoOut> result = bookingMapper.toBookingDtoOutList(emptyBookings);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toBookingDtoOut_List_WithNullList_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> bookingMapper.toBookingDtoOutList(null));
    }

    @Test
    void toEntity_WithNullBookingDtoIn_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> bookingMapper.toEntity(null, 1L));
    }

    @Test
    void toEntity_WithNullUserId_ShouldThrowUserNotFoundException() {
        Long itemId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingDtoIn bookingDtoIn = new BookingDtoIn(itemId, start, end);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(new Item(
                itemId, "", "", true, null, null
        )));
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> bookingMapper.toEntity(bookingDtoIn, null)
        );

        assertEquals("User with id null not found", exception.getMessage());
    }
}