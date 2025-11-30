package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.RequestNotFoundException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemMapperTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemMapper itemMapper;

    @Test
    void toItemDto_WithOwnerUser_ShouldReturnItemDtoWithBookings() {
        Long userId = 1L;
        Long itemId = 1L;

        User owner = new User();
        owner.setId(userId);
        owner.setName("Owner");

        Item item = new Item();
        item.setId(itemId);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);

        Booking lastBooking = new Booking();
        lastBooking.setId(1L);
        lastBooking.setEnd(LocalDateTime.now().minusDays(1));

        Booking nextBooking = new Booking();
        nextBooking.setId(2L);
        nextBooking.setStart(LocalDateTime.now().plusDays(1));

        BookingDtoOut lastBookingDto = new BookingDtoOut(1L, null, null, null, null, BookingStatus.APPROVED);
        BookingDtoOut nextBookingDto = new BookingDtoOut(2L, null, null, null, null, BookingStatus.WAITING);

        List<Comment> comments = List.of(new Comment());
        List<CommentDto> commentDtos = List.of(new CommentDto(1L, "Great item", "User", LocalDateTime.now()));

        when(bookingRepository.findByEndBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(lastBooking));
        when(bookingRepository.findByStartAfter(any(LocalDateTime.class)))
                .thenReturn(List.of(nextBooking));
        when(bookingMapper.toBookingDtoOut(lastBooking)).thenReturn(lastBookingDto);
        when(bookingMapper.toBookingDtoOut(nextBooking)).thenReturn(nextBookingDto);
        when(commentRepository.findByItemId(itemId)).thenReturn(comments);
        when(commentMapper.toCommentDto(comments)).thenReturn(commentDtos);

        ItemDto result = itemMapper.toItemDto(item, userId);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("Test Item", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertTrue(result.getAvailable());
        assertEquals(userId, result.getOwnerId());
        assertEquals(lastBookingDto, result.getLastBooking());
        assertEquals(nextBookingDto, result.getNextBooking());
        assertEquals(commentDtos, result.getComments());
    }

    @Test
    void toItemDto_WithNonOwnerUser_ShouldReturnItemDtoWithoutBookings() {
        Long ownerId = 1L;
        Long otherUserId = 2L;
        Long itemId = 1L;

        User owner = new User();
        owner.setId(ownerId);

        Item item = new Item();
        item.setId(itemId);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);

        List<Comment> comments = List.of();
        List<CommentDto> commentDtos = List.of();

        when(bookingRepository.findByEndBefore(any(LocalDateTime.class))).thenReturn(List.of());
        when(bookingRepository.findByStartAfter(any(LocalDateTime.class))).thenReturn(List.of());
        when(commentRepository.findByItemId(itemId)).thenReturn(comments);
        when(commentMapper.toCommentDto(comments)).thenReturn(commentDtos);

        ItemDto result = itemMapper.toItemDto(item, otherUserId);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertEquals(commentDtos, result.getComments());
    }

    @Test
    void toItemDto_WithRequest_ShouldIncludeRequestId() {
        Long userId = 1L;
        Long itemId = 1L;
        Long requestId = 1L;

        User owner = new User();
        owner.setId(userId);

        ItemRequest request = new ItemRequest();
        request.setId(requestId);

        Item item = new Item();
        item.setId(itemId);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);

        when(bookingRepository.findByEndBefore(any(LocalDateTime.class))).thenReturn(List.of());
        when(bookingRepository.findByStartAfter(any(LocalDateTime.class))).thenReturn(List.of());
        when(commentRepository.findByItemId(itemId)).thenReturn(List.of());
        when(commentMapper.toCommentDto(any())).thenReturn(List.of());

        ItemDto result = itemMapper.toItemDto(item, userId);

        assertNotNull(result);
        assertEquals(requestId, result.getRequestId());
    }

    @Test
    void toItemDto_WithoutRequest_ShouldHaveNullRequestId() {
        Long userId = 1L;
        Long itemId = 1L;

        User owner = new User();
        owner.setId(userId);

        Item item = new Item();
        item.setId(itemId);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(null);

        when(bookingRepository.findByEndBefore(any(LocalDateTime.class))).thenReturn(List.of());
        when(bookingRepository.findByStartAfter(any(LocalDateTime.class))).thenReturn(List.of());
        when(commentRepository.findByItemId(itemId)).thenReturn(List.of());
        when(commentMapper.toCommentDto(any())).thenReturn(List.of());

        ItemDto result = itemMapper.toItemDto(item, userId);

        assertNotNull(result);
        assertNull(result.getRequestId());
    }

    @Test
    void toItemDto_List_ShouldReturnListOfItemDtos() {
        Long userId = 1L;

        User owner = new User();
        owner.setId(userId);

        Item item1 = new Item();
        item1.setId(1L);
        item1.setOwner(owner);

        Item item2 = new Item();
        item2.setId(2L);
        item2.setOwner(owner);

        List<Item> items = List.of(item1, item2);

        when(bookingRepository.findByEndBefore(any(LocalDateTime.class))).thenReturn(List.of());
        when(bookingRepository.findByStartAfter(any(LocalDateTime.class))).thenReturn(List.of());
        when(commentRepository.findByItemId(anyLong())).thenReturn(List.of());
        when(commentMapper.toCommentDto(any())).thenReturn(List.of());

        List<ItemDto> result = itemMapper.toItemDtoList(items, userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void toEntity_WithValidData_ShouldReturnItem() {
        Long userId = 1L;
        Long requestId = 1L;

        ItemDto itemDto = new ItemDto(
                1L,
                "Test Item",
                "Test Description",
                true,
                userId,
                requestId,
                null,
                null,
                null
        );

        User user = new User();
        user.setId(userId);

        ItemRequest request = new ItemRequest();
        request.setId(requestId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        Item result = itemMapper.toEntity(itemDto, userId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Item", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertTrue(result.getAvailable());
        assertEquals(user, result.getOwner());
        assertEquals(request, result.getRequest());
    }

    @Test
    void toEntity_WithoutRequest_ShouldReturnItemWithNullRequest() {
        Long userId = 1L;

        ItemDto itemDto = new ItemDto(
                1L,
                "Test Item",
                "Test Description",
                true,
                userId,
                null,
                null,
                null,
                null
        );

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Item result = itemMapper.toEntity(itemDto, userId);

        assertNotNull(result);
        assertEquals("Test Item", result.getName());
        assertNull(result.getRequest());
    }

    @Test
    void toEntity_WithNonExistentUser_ShouldThrowUserNotFoundException() {
        Long userId = 999L;

        ItemDto itemDto = new ItemDto(
                1L,
                "Test Item",
                "Test Description",
                true,
                userId,
                null,
                null,
                null,
                null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> itemMapper.toEntity(itemDto, userId)
        );

        assertEquals("User with id 999 not found", exception.getMessage());
    }

    @Test
    void toEntity_WithNonExistentRequest_ShouldThrowRequestNotFoundException() {
        Long userId = 1L;
        Long requestId = 999L;

        ItemDto itemDto = new ItemDto(
                1L,
                "Test Item",
                "Test Description",
                true,
                userId,
                requestId,
                null,
                null,
                null
        );

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        RequestNotFoundException exception = assertThrows(
                RequestNotFoundException.class,
                () -> itemMapper.toEntity(itemDto, userId)
        );

        assertEquals("Request with id 999 not found", exception.getMessage());
    }

    @Test
    void toEntity_WithNullItemDto_ShouldThrowNullPointerException() {
        Long userId = 1L;

        assertThrows(NullPointerException.class, () -> itemMapper.toEntity(null, userId));
    }

    @Test
    void toItemDto_WithNullItem_ShouldThrowNullPointerException() {
        Long userId = 1L;

        assertThrows(NullPointerException.class, () -> itemMapper.toItemDto(null, userId));
    }

    @Test
    void toItemDto_List_WithEmptyList_ShouldReturnEmptyList() {
        List<Item> emptyList = List.of();

        List<ItemDto> result = itemMapper.toItemDtoList(emptyList, 1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toItemDto_List_WithNullList_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> itemMapper.toItemDto(null, 1L));
    }
}