package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(scripts = {"/test-schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ItemRequestRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        user1 = createUser("user1@example.com", "User One");
        user2 = createUser("user2@example.com", "User Two");
        user3 = createUser("user3@example.com", "User Three");

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);
        entityManager.flush();
    }

    @Test
    void findByRequestor_Id_WithExistingRequests_ShouldReturnUserRequests() {
        ItemRequest request1 = createItemRequest("Need a drill for home repairs", user1);
        ItemRequest request2 = createItemRequest("Need a hammer", user1);
        ItemRequest request3 = createItemRequest("Need a screwdriver", user2);

        entityManager.persist(request1);
        entityManager.persist(request2);
        entityManager.persist(request3);
        entityManager.flush();

        List<ItemRequest> result = itemRequestRepository.findByRequestor_Id(user1.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(request -> request.getRequestor().getId().equals(user1.getId())));
        assertTrue(result.stream().anyMatch(request -> request.getDescription().equals("Need a drill for home repairs")));
        assertTrue(result.stream().anyMatch(request -> request.getDescription().equals("Need a hammer")));
    }

    @Test
    void findByRequestor_Id_WithNoRequests_ShouldReturnEmptyList() {
        List<ItemRequest> result = itemRequestRepository.findByRequestor_Id(user1.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findByRequestor_Id_WithMultipleUsers_ShouldReturnOnlySpecifiedUserRequests() {
        ItemRequest request1 = createItemRequest("User1 request 1", user1);
        ItemRequest request2 = createItemRequest("User1 request 2", user1);
        ItemRequest request3 = createItemRequest("User2 request 1", user2);
        ItemRequest request4 = createItemRequest("User3 request 1", user3);

        entityManager.persist(request1);
        entityManager.persist(request2);
        entityManager.persist(request3);
        entityManager.persist(request4);
        entityManager.flush();

        List<ItemRequest> resultUser1 = itemRequestRepository.findByRequestor_Id(user1.getId());
        List<ItemRequest> resultUser2 = itemRequestRepository.findByRequestor_Id(user2.getId());
        List<ItemRequest> resultUser3 = itemRequestRepository.findByRequestor_Id(user3.getId());

        assertEquals(2, resultUser1.size());
        assertEquals(1, resultUser2.size());
        assertEquals(1, resultUser3.size());

        assertTrue(resultUser1.stream().allMatch(request -> request.getRequestor().getId().equals(user1.getId())));
        assertTrue(resultUser2.stream().allMatch(request -> request.getRequestor().getId().equals(user2.getId())));
        assertTrue(resultUser3.stream().allMatch(request -> request.getRequestor().getId().equals(user3.getId())));
    }

    @Test
    void findByRequestor_Id_WithNonExistingUserId_ShouldReturnEmptyList() {
        ItemRequest request = createItemRequest("Test request", user1);
        entityManager.persist(request);
        entityManager.flush();

        List<ItemRequest> result = itemRequestRepository.findByRequestor_Id(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void save_ShouldPersistItemRequestWithCorrectRelations() {
        ItemRequest newRequest = createItemRequest("New request for testing", user1);

        ItemRequest savedRequest = itemRequestRepository.save(newRequest);

        assertNotNull(savedRequest.getId());
        assertEquals("New request for testing", savedRequest.getDescription());
        assertEquals(user1.getId(), savedRequest.getRequestor().getId());
        assertNotNull(savedRequest.getCreated());

        ItemRequest retrieved = itemRequestRepository.findById(savedRequest.getId()).orElse(null);
        assertNotNull(retrieved);
        assertEquals(savedRequest.getId(), retrieved.getId());
        assertEquals("New request for testing", retrieved.getDescription());
        assertEquals(user1.getId(), retrieved.getRequestor().getId());
    }

    @Test
    void findById_WithExistingId_ShouldReturnItemRequest() {
        ItemRequest request = createItemRequest("Test request description", user1);
        entityManager.persist(request);
        entityManager.flush();

        ItemRequest result = itemRequestRepository.findById(request.getId()).orElse(null);

        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
        assertEquals("Test request description", result.getDescription());
        assertEquals(user1.getId(), result.getRequestor().getId());
        assertEquals(request.getCreated(), result.getCreated());
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        assertTrue(itemRequestRepository.findById(999L).isEmpty());
    }

    @Test
    void findAll_ShouldReturnAllItemRequests() {
        ItemRequest request1 = createItemRequest("Request 1", user1);
        ItemRequest request2 = createItemRequest("Request 2", user2);
        ItemRequest request3 = createItemRequest("Request 3", user3);

        entityManager.persist(request1);
        entityManager.persist(request2);
        entityManager.persist(request3);
        entityManager.flush();

        List<ItemRequest> result = itemRequestRepository.findAll();

        assertEquals(3, result.size());
    }

    @Test
    void deleteById_ShouldRemoveItemRequest() {
        ItemRequest request = createItemRequest("Request to delete", user1);
        entityManager.persist(request);
        entityManager.flush();

        Long requestId = request.getId();
        itemRequestRepository.deleteById(requestId);
        entityManager.flush();

        assertTrue(itemRequestRepository.findById(requestId).isEmpty());
    }

    @Test
    void findByRequestor_Id_ShouldMaintainRequestOrder() {
        LocalDateTime now = LocalDateTime.now();

        ItemRequest request1 = createItemRequest("First request", user1, now.minusDays(2));
        ItemRequest request2 = createItemRequest("Second request", user1, now.minusDays(1));
        ItemRequest request3 = createItemRequest("Third request", user1, now);

        entityManager.persist(request1);
        entityManager.persist(request2);
        entityManager.persist(request3);
        entityManager.flush();

        List<ItemRequest> result = itemRequestRepository.findByRequestor_Id(user1.getId());

        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getDescription().equals("First request")));
        assertTrue(result.stream().anyMatch(r -> r.getDescription().equals("Second request")));
        assertTrue(result.stream().anyMatch(r -> r.getDescription().equals("Third request")));
    }

    @Test
    void updateItemRequest_ShouldModifyExistingRequest() {
        ItemRequest request = createItemRequest("Original description", user1);
        entityManager.persist(request);
        entityManager.flush();

        request.setDescription("Updated description");
        ItemRequest updatedRequest = itemRequestRepository.save(request);

        assertEquals(request.getId(), updatedRequest.getId());
        assertEquals("Updated description", updatedRequest.getDescription());

        ItemRequest fromDb = itemRequestRepository.findById(request.getId()).orElse(null);
        assertNotNull(fromDb);
        assertEquals("Updated description", fromDb.getDescription());
    }

    @Test
    void findByRequestor_Id_WithEmptyDescription_ShouldWorkCorrectly() {
        ItemRequest request = createItemRequest("", user1);
        entityManager.persist(request);
        entityManager.flush();

        List<ItemRequest> result = itemRequestRepository.findByRequestor_Id(user1.getId());

        assertEquals(1, result.size());
        assertEquals("", result.get(0).getDescription());
    }

    @Test
    void findByRequestor_Id_WithNullDescription_ShouldWorkCorrectly() {
        ItemRequest request = new ItemRequest();
        request.setDescription(null);
        request.setRequestor(user1);
        request.setCreated(LocalDateTime.now());

        entityManager.persist(request);
        entityManager.flush();

        List<ItemRequest> result = itemRequestRepository.findByRequestor_Id(user1.getId());

        assertEquals(1, result.size());
        assertNull(result.get(0).getDescription());
    }

    @Test
    void save_ShouldSetCreationTimestamp() {
        LocalDateTime beforeSave = LocalDateTime.now().minusSeconds(1);
        ItemRequest request = createItemRequest("Test request", user1);

        ItemRequest savedRequest = itemRequestRepository.save(request);

        assertNotNull(savedRequest.getCreated());
        assertTrue(savedRequest.getCreated().isAfter(beforeSave));
        assertTrue(savedRequest.getCreated().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void findByRequestor_Id_WithLargeNumberOfRequests_ShouldHandleCorrectly() {
        for (int i = 0; i < 10; i++) {
            ItemRequest request = createItemRequest("Request " + i, user1);
            entityManager.persist(request);
        }

        ItemRequest otherUserRequest = createItemRequest("Other user request", user2);
        entityManager.persist(otherUserRequest);
        entityManager.flush();

        List<ItemRequest> result = itemRequestRepository.findByRequestor_Id(user1.getId());

        assertEquals(10, result.size());
        assertTrue(result.stream().allMatch(request -> request.getRequestor().getId().equals(user1.getId())));
    }

    @Test
    void findByRequestor_Id_ShouldReturnCorrectUserData() {
        ItemRequest request = createItemRequest("Test request", user1);
        entityManager.persist(request);
        entityManager.flush();

        List<ItemRequest> result = itemRequestRepository.findByRequestor_Id(user1.getId());

        assertEquals(1, result.size());
        assertEquals(user1.getId(), result.get(0).getRequestor().getId());
        assertEquals(user1.getName(), result.get(0).getRequestor().getName());
        assertEquals(user1.getEmail(), result.get(0).getRequestor().getEmail());
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