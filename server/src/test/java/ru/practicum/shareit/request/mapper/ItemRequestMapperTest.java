package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestMapperTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ItemRequestMapper itemRequestMapper;

    @Test
    void toEntity_WithValidData_ShouldReturnItemRequest() {
        Long userId = 1L;
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need a drill for home repairs");

        User user = new User();
        user.setId(userId);
        user.setName("Test User");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ItemRequest result = itemRequestMapper.toEntity(userId, requestDto);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("Need a drill for home repairs", result.getDescription());
        assertEquals(user, result.getRequestor());
        assertNotNull(result.getCreated());
        assertTrue(result.getCreated().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(result.getCreated().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void toEntity_WithNonExistentUser_ShouldThrowUserNotFoundException() {
        Long userId = 999L;
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need a drill");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> itemRequestMapper.toEntity(userId, requestDto)
        );

        assertEquals("User with id 999 not found", exception.getMessage());
    }

    @Test
    void toEntity_WithNullUserId_ShouldThrowUserNotFoundException() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need a drill");

        when(userRepository.findById(null)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> itemRequestMapper.toEntity(null, requestDto)
        );

        assertEquals("User with id null not found", exception.getMessage());
    }

    @Test
    void toEntity_WithNullItemRequestDto_ShouldThrowNullPointerException() {
        Long userId = 1L;

        assertThrows(NullPointerException.class, () -> itemRequestMapper.toEntity(userId, null));
    }

    @Test
    void toEntity_WithNullDescription_ShouldCreateRequestWithNullDescription() {
        Long userId = 1L;
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription(null);

        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ItemRequest result = itemRequestMapper.toEntity(userId, requestDto);

        assertNotNull(result);
        assertNull(result.getDescription());
        assertEquals(user, result.getRequestor());
    }

    @Test
    void toItemRequestDto_WithValidItemRequest_ShouldReturnDtoWithItems() {
        Long requestId = 1L;
        Long userId = 1L;

        User requestor = new User();
        requestor.setId(userId);
        requestor.setName("Requestor");

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(requestId);
        itemRequest.setDescription("Need a drill");
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now().minusDays(1));

        Item item1 = new Item();
        item1.setId(1L);
        Item item2 = new Item();
        item2.setId(2L);
        List<Item> items = List.of(item1, item2);

        ItemDto itemDto1 = new ItemDto(1L, "Drill", "Powerful drill", true, 2L, null, null, null, null);
        ItemDto itemDto2 = new ItemDto(2L, "Hammer", "Heavy hammer", true, 2L, null, null, null, null);
        List<ItemDto> itemDtos = List.of(itemDto1, itemDto2);

        ru.practicum.shareit.user.dto.UserDto userDto = new ru.practicum.shareit.user.dto.UserDto();
        userDto.setId(userId);
        userDto.setName("Requestor");
        userDto.setEmail("requestor@example.com");

        when(itemRepository.findByRequest_Id(requestId)).thenReturn(items);
        when(itemMapper.toItemDtoList(items, userId)).thenReturn(itemDtos);
        when(userMapper.toUserDto(requestor)).thenReturn(userDto);

        ItemRequestDto result = itemRequestMapper.toItemRequestDto(itemRequest);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertEquals("Need a drill", result.getDescription());
        assertEquals(userDto, result.getRequestor());
        assertEquals(itemRequest.getCreated(), result.getCreated());
        assertEquals(itemDtos, result.getItems());
        assertEquals(2, result.getItems().size());
    }

    @Test
    void toItemRequestDto_WithNoItems_ShouldReturnDtoWithEmptyItemsList() {
        Long requestId = 1L;
        Long userId = 1L;

        User requestor = new User();
        requestor.setId(userId);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(requestId);
        itemRequest.setDescription("Need a drill");
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());

        ru.practicum.shareit.user.dto.UserDto userDto = new ru.practicum.shareit.user.dto.UserDto();
        userDto.setId(userId);

        when(itemRepository.findByRequest_Id(requestId)).thenReturn(List.of());
        when(itemMapper.toItemDtoList(List.of(), userId)).thenReturn(List.of());
        when(userMapper.toUserDto(requestor)).thenReturn(userDto);

        ItemRequestDto result = itemRequestMapper.toItemRequestDto(itemRequest);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertEquals("Need a drill", result.getDescription());
        assertEquals(userDto, result.getRequestor());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void toItemRequestDto_WithNullItemRequest_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> itemRequestMapper.toItemRequestDto(null));
    }

    @Test
    void toItemRequestDto_ShouldUseRequestorIdForItemMapping() {
        Long requestId = 1L;
        Long requestorId = 5L;

        User requestor = new User();
        requestor.setId(requestorId);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(requestId);
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());

        List<Item> items = List.of(new Item());
        List<ItemDto> itemDtos = List.of(new ItemDto());

        ru.practicum.shareit.user.dto.UserDto userDto = new ru.practicum.shareit.user.dto.UserDto();

        when(itemRepository.findByRequest_Id(requestId)).thenReturn(items);
        when(itemMapper.toItemDtoList(items, requestorId)).thenReturn(itemDtos);
        when(userMapper.toUserDto(requestor)).thenReturn(userDto);

        ItemRequestDto result = itemRequestMapper.toItemRequestDto(itemRequest);

        assertNotNull(result);
        verify(itemMapper).toItemDtoList(items, requestorId);
    }

    @Test
    void toItemRequestDto_List_WithValidRequests_ShouldReturnListOfDtos() {
        User requestor1 = new User();
        requestor1.setId(1L);
        User requestor2 = new User();
        requestor2.setId(2L);

        ItemRequest request1 = new ItemRequest();
        request1.setId(1L);
        request1.setDescription("First request");
        request1.setRequestor(requestor1);
        request1.setCreated(LocalDateTime.now().minusDays(2));

        ItemRequest request2 = new ItemRequest();
        request2.setId(2L);
        request2.setDescription("Second request");
        request2.setRequestor(requestor2);
        request2.setCreated(LocalDateTime.now().minusDays(1));

        List<ItemRequest> requests = List.of(request1, request2);

        ItemRequestDto dto1 = new ItemRequestDto(1L, "First request", null, request1.getCreated(), List.of());
        ItemRequestDto dto2 = new ItemRequestDto(2L, "Second request", null, request2.getCreated(), List.of());

        when(itemRepository.findByRequest_Id(1L)).thenReturn(List.of());
        when(itemRepository.findByRequest_Id(2L)).thenReturn(List.of());
        when(itemMapper.toItemDtoList(List.of(), 1L)).thenReturn(List.of());
        when(itemMapper.toItemDtoList(List.of(), 2L)).thenReturn(List.of());
        when(userMapper.toUserDto(requestor1)).thenReturn(new ru.practicum.shareit.user.dto.UserDto());
        when(userMapper.toUserDto(requestor2)).thenReturn(new ru.practicum.shareit.user.dto.UserDto());

        List<ItemRequestDto> result = itemRequestMapper.toItemRequestDtoList(requests);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("First request", result.get(0).getDescription());
        assertEquals(2L, result.get(1).getId());
        assertEquals("Second request", result.get(1).getDescription());
    }

    @Test
    void toItemRequestDto_List_WithEmptyList_ShouldReturnEmptyList() {
        List<ItemRequest> emptyRequests = List.of();

        List<ItemRequestDto> result = itemRequestMapper.toItemRequestDtoList(emptyRequests);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toItemRequestDto_List_WithNullList_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> itemRequestMapper.toItemRequestDtoList((List<ItemRequest>) null));
    }

    @Test
    void toItemRequestDto_WithItemsFromDifferentOwners_ShouldMapCorrectly() {
        Long requestId = 1L;
        Long requestorId = 1L;
        Long itemOwnerId = 2L;

        User requestor = new User();
        requestor.setId(requestorId);

        User itemOwner = new User();
        itemOwner.setId(itemOwnerId);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(requestId);
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());

        Item item = new Item();
        item.setId(1L);
        item.setOwner(itemOwner);
        List<Item> items = List.of(item);

        ItemDto itemDto = new ItemDto(1L, "Drill", "Description", true, itemOwnerId, null, null, null, null);
        List<ItemDto> itemDtos = List.of(itemDto);

        ru.practicum.shareit.user.dto.UserDto userDto = new ru.practicum.shareit.user.dto.UserDto();

        when(itemRepository.findByRequest_Id(requestId)).thenReturn(items);
        when(itemMapper.toItemDtoList(items, requestorId)).thenReturn(itemDtos);
        when(userMapper.toUserDto(requestor)).thenReturn(userDto);

        ItemRequestDto result = itemRequestMapper.toItemRequestDto(itemRequest);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(itemDto, result.getItems().get(0));
    }

    @Test
    void toEntity_ShouldSetCurrentTimestamp() {
        Long userId = 1L;
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Test request");

        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ItemRequest result = itemRequestMapper.toEntity(userId, requestDto);

        assertNotNull(result.getCreated());
        LocalDateTime now = LocalDateTime.now();
        assertTrue(result.getCreated().isBefore(now.plusSeconds(1)));
        assertTrue(result.getCreated().isAfter(now.minusSeconds(1)));
    }

    @Test
    void toItemRequestDto_ShouldPreserveAllFields() {
        Long requestId = 1L;
        Long userId = 1L;
        String description = "Detailed description of needed item";
        LocalDateTime created = LocalDateTime.now().minusHours(5);

        User requestor = new User();
        requestor.setId(userId);
        requestor.setName("John Doe");

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(requestId);
        itemRequest.setDescription(description);
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(created);

        ru.practicum.shareit.user.dto.UserDto userDto = new ru.practicum.shareit.user.dto.UserDto();
        userDto.setId(userId);
        userDto.setName("John Doe");

        when(itemRepository.findByRequest_Id(requestId)).thenReturn(List.of());
        when(itemMapper.toItemDtoList(List.of(), userId)).thenReturn(List.of());
        when(userMapper.toUserDto(requestor)).thenReturn(userDto);

        ItemRequestDto result = itemRequestMapper.toItemRequestDto(itemRequest);

        assertEquals(requestId, result.getId());
        assertEquals(description, result.getDescription());
        assertEquals(userDto, result.getRequestor());
        assertEquals(created, result.getCreated());
        assertTrue(result.getItems().isEmpty());
    }
}