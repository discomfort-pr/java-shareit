package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.RequestNotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void addItemRequest_WithValidRequest_ShouldSaveAndReturnRequest() {
        ItemRequest request = new ItemRequest();
        request.setDescription("Need a drill");

        ItemRequest savedRequest = new ItemRequest();
        savedRequest.setId(1L);
        savedRequest.setDescription("Need a drill");

        when(itemRequestRepository.save(request)).thenReturn(savedRequest);

        ItemRequest result = itemRequestService.addItemRequest(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Need a drill", result.getDescription());
        verify(itemRequestRepository).save(request);
    }

    @Test
    void getUserItemRequests_WithExistingRequests_ShouldReturnSortedRequests() {
        Long userId = 1L;

        ItemRequest request1 = createItemRequest(1L, "First request", LocalDateTime.now().minusDays(2));
        ItemRequest request2 = createItemRequest(2L, "Second request", LocalDateTime.now().minusDays(1));
        ItemRequest request3 = createItemRequest(3L, "Third request", LocalDateTime.now());

        List<ItemRequest> requests = List.of(request1, request2, request3);

        when(itemRequestRepository.findByRequestor_Id(userId)).thenReturn(requests);

        List<ItemRequest> result = itemRequestService.getUserItemRequests(userId);

        assertNotNull(result);
        assertEquals(3, result.size());
        // Проверяем сортировку по убыванию даты создания (новые первыми)
        assertEquals(3L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertEquals(1L, result.get(2).getId());
        verify(itemRequestRepository).findByRequestor_Id(userId);
    }

    @Test
    void getUserItemRequests_WithNoRequests_ShouldReturnEmptyList() {
        Long userId = 1L;

        when(itemRequestRepository.findByRequestor_Id(userId)).thenReturn(List.of());

        List<ItemRequest> result = itemRequestService.getUserItemRequests(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRequestRepository).findByRequestor_Id(userId);
    }

    @Test
    void getUserItemRequests_WithSingleRequest_ShouldReturnSingleItemList() {
        Long userId = 1L;

        ItemRequest request = createItemRequest(1L, "Single request", LocalDateTime.now());
        when(itemRequestRepository.findByRequestor_Id(userId)).thenReturn(List.of(request));

        List<ItemRequest> result = itemRequestService.getUserItemRequests(userId);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Single request", result.get(0).getDescription());
    }

    @Test
    void getAllRequests_WithExistingRequests_ShouldReturnAllSortedRequests() {
        ItemRequest request1 = createItemRequest(1L, "Old request", LocalDateTime.now().minusDays(3));
        ItemRequest request2 = createItemRequest(2L, "Middle request", LocalDateTime.now().minusDays(1));
        ItemRequest request3 = createItemRequest(3L, "New request", LocalDateTime.now());

        List<ItemRequest> requests = List.of(request1, request2, request3);

        when(itemRequestRepository.findAll()).thenReturn(requests);

        List<ItemRequest> result = itemRequestService.getAllRequests();

        assertNotNull(result);
        assertEquals(3, result.size());
        // Проверяем сортировку по убыванию даты создания (новые первыми)
        assertEquals(3L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertEquals(1L, result.get(2).getId());
        verify(itemRequestRepository).findAll();
    }

    @Test
    void getAllRequests_WithNoRequests_ShouldReturnEmptyList() {
        when(itemRequestRepository.findAll()).thenReturn(List.of());

        List<ItemRequest> result = itemRequestService.getAllRequests();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRequestRepository).findAll();
    }

    @Test
    void getAllRequests_WithSameCreationDate_ShouldMaintainOrder() {
        LocalDateTime sameTime = LocalDateTime.now();

        ItemRequest request1 = createItemRequest(1L, "Request 1", sameTime);
        ItemRequest request2 = createItemRequest(2L, "Request 2", sameTime);
        ItemRequest request3 = createItemRequest(3L, "Request 3", sameTime);

        List<ItemRequest> requests = List.of(request1, request2, request3);

        when(itemRequestRepository.findAll()).thenReturn(requests);

        List<ItemRequest> result = itemRequestService.getAllRequests();

        assertEquals(3, result.size());
        // При одинаковом времени порядок может быть любым, но все должны присутствовать
        assertTrue(result.stream().anyMatch(r -> r.getId().equals(1L)));
        assertTrue(result.stream().anyMatch(r -> r.getId().equals(2L)));
        assertTrue(result.stream().anyMatch(r -> r.getId().equals(3L)));
    }

    @Test
    void getItemRequest_WithExistingId_ShouldReturnRequest() {
        Long requestId = 1L;
        ItemRequest expectedRequest = createItemRequest(requestId, "Test request", LocalDateTime.now());

        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(expectedRequest));

        ItemRequest result = itemRequestService.getItemRequest(requestId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertEquals("Test request", result.getDescription());
        verify(itemRequestRepository).findById(requestId);
    }

    @Test
    void getItemRequest_WithNonExistingId_ShouldThrowRequestNotFoundException() {
        Long requestId = 999L;

        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        RequestNotFoundException exception = assertThrows(
                RequestNotFoundException.class,
                () -> itemRequestService.getItemRequest(requestId)
        );

        assertEquals("Request with id 999 not found", exception.getMessage());
        verify(itemRequestRepository).findById(requestId);
    }

    @Test
    void getItemRequest_WithNullId_ShouldThrowRequestNotFoundException() {
        when(itemRequestRepository.findById(null)).thenReturn(Optional.empty());

        RequestNotFoundException exception = assertThrows(
                RequestNotFoundException.class,
                () -> itemRequestService.getItemRequest(null)
        );

        assertEquals("Request with id null not found", exception.getMessage());
    }

    @Test
    void getUserItemRequests_ShouldReturnOnlyUserRequests() {
        Long userId = 1L;
        Long otherUserId = 2L;

        ItemRequest userRequest1 = createItemRequest(1L, "User request 1", LocalDateTime.now().minusDays(2));
        ItemRequest userRequest2 = createItemRequest(2L, "User request 2", LocalDateTime.now().minusDays(1));
        ItemRequest otherUserRequest = createItemRequest(3L, "Other user request", LocalDateTime.now());

        // Репозиторий возвращает только запросы указанного пользователя
        when(itemRequestRepository.findByRequestor_Id(userId)).thenReturn(List.of(userRequest1, userRequest2));

        List<ItemRequest> result = itemRequestService.getUserItemRequests(userId);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(request ->
                request.getDescription().startsWith("User request")));
        assertTrue(result.stream().noneMatch(request ->
                request.getDescription().equals("Other user request")));
    }

    @Test
    void getAllRequests_ShouldReturnRequestsFromAllUsers() {
        ItemRequest request1 = createItemRequest(1L, "User1 request", LocalDateTime.now().minusDays(2));
        ItemRequest request2 = createItemRequest(2L, "User2 request", LocalDateTime.now().minusDays(1));
        ItemRequest request3 = createItemRequest(3L, "User3 request", LocalDateTime.now());

        when(itemRequestRepository.findAll()).thenReturn(List.of(request1, request2, request3));

        List<ItemRequest> result = itemRequestService.getAllRequests();

        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getDescription().equals("User1 request")));
        assertTrue(result.stream().anyMatch(r -> r.getDescription().equals("User2 request")));
        assertTrue(result.stream().anyMatch(r -> r.getDescription().equals("User3 request")));
    }

    private ItemRequest createItemRequest(Long id, String description, LocalDateTime created) {
        ItemRequest request = new ItemRequest();
        request.setId(id);
        request.setDescription(description);
        request.setCreated(created);
        return request;
    }
}