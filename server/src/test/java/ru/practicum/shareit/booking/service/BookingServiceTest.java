package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingCategory;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.InternalValidationException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void addBooking_WithValidData_ShouldReturnSavedBooking() {
        // Given
        Long userId = 1L;
        Long itemId = 1L;

        User user = new User();
        user.setId(userId);
        user.setName("Test User");

        User owner = new User();
        owner.setId(2L);
        owner.setName("Owner");

        Item item = new Item();
        item.setId(itemId);
        item.setName("Test Item");
        item.setAvailable(true);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = new Booking();
        savedBooking.setId(1L);
        savedBooking.setStart(booking.getStart());
        savedBooking.setEnd(booking.getEnd());
        savedBooking.setItem(item);
        savedBooking.setBooker(user);
        savedBooking.setStatus(BookingStatus.WAITING);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        Booking result = bookingService.addBooking(userId, booking);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
        verify(userRepository).findById(userId);
        verify(itemRepository).findById(itemId);
        verify(bookingRepository).save(booking);
    }

    @Test
    void addBooking_WithNonExistentUser_ShouldThrowUserNotFoundException() {
        Long userId = 999L;
        Booking booking = new Booking();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> bookingService.addBooking(userId, booking)
        );

        assertEquals("User with id 999 not found", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void addBooking_WithNonExistentItem_ShouldThrowItemNotFoundException() {
        Long userId = 1L;
        Long itemId = 999L;

        User user = new User();
        user.setId(userId);

        Item item = new Item();
        item.setId(itemId);

        Booking booking = new Booking();
        booking.setItem(item);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> bookingService.addBooking(userId, booking)
        );

        assertEquals("Item with id 999 not found", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void addBooking_WithUnavailableItem_ShouldThrowInternalValidationException() {
        Long userId = 1L;
        Long itemId = 1L;

        User user = new User();
        user.setId(userId);

        Item item = new Item();
        item.setId(itemId);
        item.setAvailable(false);

        Booking booking = new Booking();
        booking.setItem(item);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        InternalValidationException exception = assertThrows(
                InternalValidationException.class,
                () -> bookingService.addBooking(userId, booking)
        );

        assertEquals("Cannot book unavailable item", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void addBooking_WithEqualStartAndEndTime_ShouldThrowInternalValidationException() {
        Long userId = 1L;
        Long itemId = 1L;
        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setId(userId);

        Item item = new Item();
        item.setId(itemId);
        item.setAvailable(true);

        Booking booking = new Booking();
        booking.setStart(now);
        booking.setEnd(now);
        booking.setItem(item);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        InternalValidationException exception = assertThrows(
                InternalValidationException.class,
                () -> bookingService.addBooking(userId, booking)
        );

        assertEquals("Start time cannot be equal to end time", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void processBooking_WithApprovedTrue_ShouldUpdateStatusToApproved() {
        Long bookingId = 1L;
        Long userId = 1L;
        Long itemId = 1L;

        User owner = new User();
        owner.setId(userId);

        User booker = new User();
        booker.setId(2L);

        Item item = new Item();
        item.setId(itemId);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking result = bookingService.processBooking(bookingId, userId, "true");

        assertEquals(BookingStatus.APPROVED, result.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void processBooking_WithApprovedFalse_ShouldUpdateStatusToRejected() {
        Long bookingId = 1L;
        Long userId = 1L;
        Long itemId = 1L;

        User owner = new User();
        owner.setId(userId);

        User booker = new User();
        booker.setId(2L);

        Item item = new Item();
        item.setId(itemId);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking result = bookingService.processBooking(bookingId, userId, "false");

        assertEquals(BookingStatus.REJECTED, result.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void processBooking_WithInvalidApprovedParameter_ShouldThrowInternalValidationException() {
        Long bookingId = 1L;
        Long userId = 1L;
        Long itemId = 1L;

        User owner = new User();
        owner.setId(userId);

        Item item = new Item();
        item.setId(itemId);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItem(item);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        InternalValidationException exception = assertThrows(
                InternalValidationException.class,
                () -> bookingService.processBooking(bookingId, userId, "invalid")
        );

        assertEquals("Invalid 'approved' parameter value (can be true or false)", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void processBooking_ByNonOwner_ShouldThrowInternalValidationException() {
        Long bookingId = 1L;
        Long userId = 999L;
        Long itemId = 1L;

        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setId(itemId);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItem(item);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        InternalValidationException exception = assertThrows(
                InternalValidationException.class,
                () -> bookingService.processBooking(bookingId, userId, "true")
        );

        assertEquals("Item requests can be approved by item owner", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void getBooking_ByBooker_ShouldReturnBooking() {
        Long bookingId = 1L;
        Long userId = 1L; // Booker ID
        Long itemId = 1L;

        User booker = new User();
        booker.setId(userId);

        User owner = new User();
        owner.setId(2L);

        Item item = new Item();
        item.setId(itemId);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItem(item);
        booking.setBooker(booker);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        Booking result = bookingService.getBooking(userId, bookingId);

        assertEquals(bookingId, result.getId());
    }

    @Test
    void getBooking_ByOwner_ShouldReturnBooking() {
        Long bookingId = 1L;
        Long userId = 1L;
        Long itemId = 1L;

        User booker = new User();
        booker.setId(2L);

        User owner = new User();
        owner.setId(userId);

        Item item = new Item();
        item.setId(itemId);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItem(item);
        booking.setBooker(booker);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        Booking result = bookingService.getBooking(userId, bookingId);

        assertEquals(bookingId, result.getId());
    }

    @Test
    void getBooking_ByUnauthorizedUser_ShouldThrowInternalValidationException() {
        Long bookingId = 1L;
        Long userId = 999L; // Neither owner nor booker
        Long itemId = 1L;

        User booker = new User();
        booker.setId(2L);

        User owner = new User();
        owner.setId(3L);

        Item item = new Item();
        item.setId(itemId);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItem(item);
        booking.setBooker(booker);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        InternalValidationException exception = assertThrows(
                InternalValidationException.class,
                () -> bookingService.getBooking(userId, bookingId)
        );

        assertEquals("You must be a booker or item owner to get booking info", exception.getMessage());
    }

    @Test
    void getUserBookings_WithAllCategory_ShouldReturnAllBookings() {
        Long userId = 1L;
        List<Booking> expectedBookings = List.of(new Booking(), new Booking());

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(bookingRepository.findAllByBookerId(userId)).thenReturn(expectedBookings);

        List<Booking> result = bookingService.getUserBookings(userId, BookingCategory.ALL);

        assertEquals(expectedBookings, result);
        verify(bookingRepository).findAllByBookerId(userId);
    }

    @Test
    void getUserItemsBookings_WithAllCategory_ShouldReturnAllBookings() {
        Long userId = 1L;
        List<Booking> expectedBookings = List.of(new Booking(), new Booking());

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(itemRepository.findByOwnerId(userId)).thenReturn(List.of(new Item(), new Item()));
        when(bookingRepository.findAllByItemIdIn(anyList())).thenReturn(expectedBookings);

        List<Booking> result = bookingService.getUserItemsBookings(userId, BookingCategory.ALL);

        assertEquals(expectedBookings, result);
        verify(bookingRepository).findAllByItemIdIn(any());
    }
}