package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(scripts = {"/test-schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ItemRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemRepository itemRepository;

    private User owner1;
    private User owner2;
    private User requestor;
    private ItemRequest request1;
    private ItemRequest request2;

    @BeforeEach
    void setUp() {
        owner1 = createUser("owner1@example.com", "Owner One");
        owner2 = createUser("owner2@example.com", "Owner Two");
        requestor = createUser("requestor@example.com", "Requestor");

        entityManager.persist(owner1);
        entityManager.persist(owner2);
        entityManager.persist(requestor);

        request1 = createItemRequest("Need item 1", requestor);
        request2 = createItemRequest("Need item 2", requestor);

        entityManager.persist(request1);
        entityManager.persist(request2);

        entityManager.flush();
    }

    @Test
    void findByOwnerId_ShouldReturnItemsForOwner() {
        Item item1 = createItem("Item 1", "Description 1", owner1, true, null);
        Item item2 = createItem("Item 2", "Description 2", owner1, true, null);
        Item item3 = createItem("Item 3", "Description 3", owner2, true, null);

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(item3);
        entityManager.flush();

        List<Item> result = itemRepository.findByOwnerId(owner1.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(item -> item.getOwner().getId().equals(owner1.getId())));
        assertTrue(result.stream().anyMatch(item -> item.getName().equals("Item 1")));
        assertTrue(result.stream().anyMatch(item -> item.getName().equals("Item 2")));
    }

    @Test
    void findByOwnerId_WithNoItems_ShouldReturnEmptyList() {
        List<Item> result = itemRepository.findByOwnerId(owner1.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findByText_WithMatchingName_ShouldReturnItems() {
        Item item1 = createItem("Drill Machine", "Powerful drill", owner1, true, null);
        Item item2 = createItem("Hammer", "Heavy hammer", owner1, true, null);
        Item item3 = createItem("Screwdriver", "Phillips screwdriver", owner2, false, null);

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(item3);
        entityManager.flush();

        List<Item> result = itemRepository.findByText("drill");

        assertEquals(1, result.size());
        assertEquals("Drill Machine", result.get(0).getName());
        assertTrue(result.get(0).getAvailable());
    }

    @Test
    void findByText_WithMatchingDescription_ShouldReturnItems() {
        Item item1 = createItem("Tool 1", "Electric drill for construction", owner1, true, null);
        Item item2 = createItem("Tool 2", "Manual hammer", owner1, true, null);
        Item item3 = createItem("Tool 3", "Broken drill", owner2, false, null);

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(item3);
        entityManager.flush();

        List<Item> result = itemRepository.findByText("drill");

        assertEquals(1, result.size());
        assertEquals("Tool 1", result.get(0).getName());
        assertTrue(result.get(0).getDescription().contains("drill"));
    }

    @Test
    void findByText_WithCaseInsensitiveSearch_ShouldReturnItems() {
        Item item1 = createItem("DRILL Machine", "POWERFUL tool", owner1, true, null);
        Item item2 = createItem("hammer", "small tool", owner1, true, null);

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.flush();

        List<Item> result = itemRepository.findByText("drill");

        assertEquals(1, result.size());
        assertEquals("DRILL Machine", result.get(0).getName());
    }

    @Test
    void findByText_WithUnavailableItems_ShouldNotReturnThem() {
        Item item1 = createItem("Drill", "Powerful", owner1, true, null);
        Item item2 = createItem("Hammer", "Heavy", owner1, false, null);

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.flush();

        List<Item> result = itemRepository.findByText("hammer");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByText_WithEmptyText_ShouldReturnItems() {
        Item item1 = createItem("Drill", "Powerful", owner1, true, null);
        entityManager.persist(item1);
        entityManager.flush();

        List<Item> result = itemRepository.findByText("");

        assertFalse(result.isEmpty());
    }

    @Test
    void findByText_WithNoMatches_ShouldEmptyList() {
        Item item1 = createItem("Drill", "Powerful", owner1, true, null);
        Item item2 = createItem("Hammer", "Heavy", owner1, true, null);

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.flush();

        List<Item> result = itemRepository.findByText("saw");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByText_WithPartialMatch_ShouldReturnItems() {
        Item item1 = createItem("Electric Drill", "Very powerful", owner1, true, null);
        Item item2 = createItem("Cordless Screwdriver", "Battery operated", owner1, true, null);

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.flush();

        List<Item> result = itemRepository.findByText("lectric");

        assertEquals(1, result.size());
        assertEquals("Electric Drill", result.get(0).getName());
    }

    @Test
    void findByRequest_Id_ShouldReturnItemsForRequest() {
        Item item1 = createItem("Item for request 1", "Description 1", owner1, true, request1);
        Item item2 = createItem("Item for request 1 again", "Description 2", owner2, true, request1);
        Item item3 = createItem("Item for request 2", "Description 3", owner1, true, request2);

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(item3);
        entityManager.flush();

        List<Item> result = itemRepository.findByRequest_Id(request1.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(item -> item.getRequest().getId().equals(request1.getId())));
        assertTrue(result.stream().anyMatch(item -> item.getName().equals("Item for request 1")));
        assertTrue(result.stream().anyMatch(item -> item.getName().equals("Item for request 1 again")));
    }

    @Test
    void findByRequest_Id_WithNoItems_ShouldReturnEmptyList() {
        List<Item> result = itemRepository.findByRequest_Id(request1.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findByRequest_Id_WithNullRequest_ShouldReturnEmptyList() {
        Item item1 = createItem("Item without request", "Description", owner1, true, null);
        entityManager.persist(item1);
        entityManager.flush();

        List<Item> result = itemRepository.findByRequest_Id(request1.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void save_ShouldPersistItemWithCorrectRelations() {
        Item newItem = createItem("New Item", "New Description", owner1, true, request1);

        Item savedItem = itemRepository.save(newItem);

        assertNotNull(savedItem.getId());
        assertEquals("New Item", savedItem.getName());
        assertEquals("New Description", savedItem.getDescription());
        assertTrue(savedItem.getAvailable());
        assertEquals(owner1.getId(), savedItem.getOwner().getId());
        assertEquals(request1.getId(), savedItem.getRequest().getId());

        Item retrieved = itemRepository.findById(savedItem.getId()).orElse(null);
        assertNotNull(retrieved);
        assertEquals(savedItem.getId(), retrieved.getId());
        assertEquals("New Item", retrieved.getName());
    }

    @Test
    void findById_WithExistingId_ShouldReturnItem() {
        Item item = createItem("Test Item", "Test Description", owner1, true, null);
        entityManager.persist(item);
        entityManager.flush();

        Item result = itemRepository.findById(item.getId()).orElse(null);

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals("Test Item", result.getName());
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        assertTrue(itemRepository.findById(999L).isEmpty());
    }

    @Test
    void findAll_ShouldReturnAllItems() {
        Item item1 = createItem("Item 1", "Desc 1", owner1, true, null);
        Item item2 = createItem("Item 2", "Desc 2", owner2, true, null);
        Item item3 = createItem("Item 3", "Desc 3", owner1, false, null);

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(item3);
        entityManager.flush();

        List<Item> result = itemRepository.findAll();

        assertEquals(3, result.size());
    }

    @Test
    void deleteById_ShouldRemoveItem() {
        Item item = createItem("To Delete", "Description", owner1, true, null);
        entityManager.persist(item);
        entityManager.flush();

        Long itemId = item.getId();
        itemRepository.deleteById(itemId);
        entityManager.flush();

        assertTrue(itemRepository.findById(itemId).isEmpty());
    }

    private User createUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }

    private ItemRequest createItemRequest(String description, User requestor) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        return request;
    }

    private Item createItem(String name, String description, User owner, Boolean available, ItemRequest request) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(owner);
        item.setRequest(request);
        return item;
    }
}