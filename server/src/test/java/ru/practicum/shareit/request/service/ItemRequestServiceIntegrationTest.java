package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.RequestNotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Sql(scripts = {"/test-schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestServiceImpl itemRequestService;

    @Autowired
    private ru.practicum.shareit.request.repository.ItemRequestRepository itemRequestRepository;

    @Autowired
    private ru.practicum.shareit.user.repository.UserRepository userRepository;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        user1 = createUser("user1@example.com", "User One");
        user2 = createUser("user2@example.com", "User Two");
        user3 = createUser("user3@example.com", "User Three");

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
    }

    @Test
    void addItemRequest_ShouldPersistRequestInDatabase() {
        ItemRequest request = createItemRequest("Need a drill for home repairs", user1);

        ItemRequest result = itemRequestService.addItemRequest(request);

        assertNotNull(result.getId());
        assertEquals("Need a drill for home repairs", result.getDescription());
        assertEquals(user1.getId(), result.getRequestor().getId());
        assertNotNull(result.getCreated());

        ItemRequest fromDb = itemRequestRepository.findById(result.getId()).orElse(null);
        assertNotNull(fromDb);
        assertEquals(result.getId(), fromDb.getId());
        assertEquals("Need a drill for home repairs", fromDb.getDescription());
        assertEquals(user1.getId(), fromDb.getRequestor().getId());
    }

    @Test
    void getUserItemRequests_ShouldReturnOnlyUserRequestsSortedByDateDesc() {
        LocalDateTime now = LocalDateTime.now();

        ItemRequest request1 = createItemRequest("Old request", user1, now.minusDays(2));
        ItemRequest request2 = createItemRequest("Middle request", user1, now.minusDays(1));
        ItemRequest request3 = createItemRequest("New request", user1, now);
        ItemRequest otherUserRequest = createItemRequest("Other user request", user2, now);

        itemRequestRepository.save(request1);
        itemRequestRepository.save(request2);
        itemRequestRepository.save(request3);
        itemRequestRepository.save(otherUserRequest);

        List<ItemRequest> result = itemRequestService.getUserItemRequests(user1.getId());

        assertEquals(3, result.size());
        // Проверяем сортировку по убыванию даты (новые первыми)
        assertEquals("New request", result.get(0).getDescription());
        assertEquals("Middle request", result.get(1).getDescription());
        assertEquals("Old request", result.get(2).getDescription());

        // Проверяем, что запросы других пользователей не включены
        assertTrue(result.stream().noneMatch(req -> req.getDescription().equals("Other user request")));
    }

    @Test
    void getUserItemRequests_WithNoRequests_ShouldReturnEmptyList() {
        List<ItemRequest> result = itemRequestService.getUserItemRequests(user1.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllRequests_ShouldReturnAllRequestsSortedByDateDesc() {
        LocalDateTime now = LocalDateTime.now();

        ItemRequest request1 = createItemRequest("User1 old request", user1, now.minusDays(3));
        ItemRequest request2 = createItemRequest("User2 request", user2, now.minusDays(1));
        ItemRequest request3 = createItemRequest("User1 new request", user1, now);
        ItemRequest request4 = createItemRequest("User3 request", user3, now.minusDays(2));

        itemRequestRepository.save(request1);
        itemRequestRepository.save(request2);
        itemRequestRepository.save(request3);
        itemRequestRepository.save(request4);

        List<ItemRequest> result = itemRequestService.getAllRequests();

        assertEquals(4, result.size());
        // Проверяем сортировку по убыванию даты (новые первыми)
        assertEquals("User1 new request", result.get(0).getDescription());
        assertEquals("User2 request", result.get(1).getDescription());
        assertEquals("User3 request", result.get(2).getDescription());
        assertEquals("User1 old request", result.get(3).getDescription());
    }

    @Test
    void getAllRequests_WithNoRequests_ShouldReturnEmptyList() {
        List<ItemRequest> result = itemRequestService.getAllRequests();

        assertTrue(result.isEmpty());
    }

    @Test
    void getItemRequest_WithExistingId_ShouldReturnRequest() {
        ItemRequest request = createItemRequest("Test request", user1);
        ItemRequest savedRequest = itemRequestRepository.save(request);

        ItemRequest result = itemRequestService.getItemRequest(savedRequest.getId());

        assertNotNull(result);
        assertEquals(savedRequest.getId(), result.getId());
        assertEquals("Test request", result.getDescription());
        assertEquals(user1.getId(), result.getRequestor().getId());
        assertEquals(savedRequest.getCreated(), result.getCreated());
    }

    @Test
    void getItemRequest_WithNonExistingId_ShouldThrowRequestNotFoundException() {
        assertThrows(RequestNotFoundException.class, () -> itemRequestService.getItemRequest(999L));
    }

    @Test
    void addItemRequest_ShouldSetCreationTimestamp() {
        LocalDateTime beforeSave = LocalDateTime.now().minusSeconds(1);
        ItemRequest request = createItemRequest("Test request", user1);

        ItemRequest result = itemRequestService.addItemRequest(request);

        assertNotNull(result.getCreated());
        assertTrue(result.getCreated().isAfter(beforeSave));
        assertTrue(result.getCreated().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void getUserItemRequests_WithMultipleUsers_ShouldReturnCorrectRequestsForEachUser() {
        ItemRequest user1Request1 = createItemRequest("User1 request 1", user1);
        ItemRequest user1Request2 = createItemRequest("User1 request 2", user1);
        ItemRequest user2Request1 = createItemRequest("User2 request 1", user2);
        ItemRequest user3Request1 = createItemRequest("User3 request 1", user3);

        itemRequestRepository.save(user1Request1);
        itemRequestRepository.save(user1Request2);
        itemRequestRepository.save(user2Request1);
        itemRequestRepository.save(user3Request1);

        List<ItemRequest> user1Results = itemRequestService.getUserItemRequests(user1.getId());
        List<ItemRequest> user2Results = itemRequestService.getUserItemRequests(user2.getId());
        List<ItemRequest> user3Results = itemRequestService.getUserItemRequests(user3.getId());

        assertEquals(2, user1Results.size());
        assertEquals(1, user2Results.size());
        assertEquals(1, user3Results.size());

        assertTrue(user1Results.stream().allMatch(req -> req.getRequestor().getId().equals(user1.getId())));
        assertTrue(user2Results.stream().allMatch(req -> req.getRequestor().getId().equals(user2.getId())));
        assertTrue(user3Results.stream().allMatch(req -> req.getRequestor().getId().equals(user3.getId())));
    }

    @Test
    void getAllRequests_ShouldIncludeRequestsFromAllUsers() {
        ItemRequest user1Request = createItemRequest("User1 request", user1);
        ItemRequest user2Request = createItemRequest("User2 request", user2);
        ItemRequest user3Request = createItemRequest("User3 request", user3);

        itemRequestRepository.save(user1Request);
        itemRequestRepository.save(user2Request);
        itemRequestRepository.save(user3Request);

        List<ItemRequest> result = itemRequestService.getAllRequests();

        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(req -> req.getRequestor().getId().equals(user1.getId())));
        assertTrue(result.stream().anyMatch(req -> req.getRequestor().getId().equals(user2.getId())));
        assertTrue(result.stream().anyMatch(req -> req.getRequestor().getId().equals(user3.getId())));
    }

    @Test
    void addItemRequest_WithEmptyDescription_ShouldWorkCorrectly() {
        ItemRequest request = createItemRequest("", user1);

        ItemRequest result = itemRequestService.addItemRequest(request);

        assertNotNull(result.getId());
        assertEquals("", result.getDescription());
        assertEquals(user1.getId(), result.getRequestor().getId());
    }

    @Test
    void addItemRequest_WithNullDescription_ShouldWorkCorrectly() {
        ItemRequest request = new ItemRequest();
        request.setDescription(null);
        request.setRequestor(user1);
        request.setCreated(LocalDateTime.now());

        ItemRequest result = itemRequestService.addItemRequest(request);

        assertNotNull(result.getId());
        assertNull(result.getDescription());
        assertEquals(user1.getId(), result.getRequestor().getId());
    }

    @Test
    void getItemRequest_ShouldReturnCompleteRequestData() {
        ItemRequest originalRequest = createItemRequest("Detailed request description", user1);
        ItemRequest savedRequest = itemRequestRepository.save(originalRequest);

        ItemRequest result = itemRequestService.getItemRequest(savedRequest.getId());

        assertEquals(savedRequest.getId(), result.getId());
        assertEquals("Detailed request description", result.getDescription());
        assertEquals(user1.getId(), result.getRequestor().getId());
        assertEquals(user1.getName(), result.getRequestor().getName());
        assertEquals(user1.getEmail(), result.getRequestor().getEmail());
        assertEquals(savedRequest.getCreated(), result.getCreated());
    }

    private User createUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }

    private ItemRequest createItemRequest(String description, User requestor) {
        return createItemRequest(description, requestor, LocalDateTime.now());
    }

    private ItemRequest createItemRequest(String description, User requestor, LocalDateTime created) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(requestor);
        request.setCreated(created);
        return request;
    }
}