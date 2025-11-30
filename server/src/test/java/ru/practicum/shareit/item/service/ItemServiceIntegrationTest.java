package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.InternalValidationException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Sql(scripts = {"/test-schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ItemServiceIntegrationTest {

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private ru.practicum.shareit.user.repository.UserRepository userRepository;

    @Autowired
    private ru.practicum.shareit.item.repository.ItemRepository itemRepository;

    private User owner;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        owner = createUser("owner@example.com", "Owner");
        anotherUser = createUser("another@example.com", "Another User");

        userRepository.save(owner);
        userRepository.save(anotherUser);
    }

    @Test
    void getAllItems_ShouldReturnAllItemsFromDatabase() {
        Item item1 = createItem("Item 1", "Description 1", owner, true);
        Item item2 = createItem("Item 2", "Description 2", owner, true);

        itemRepository.save(item1);
        itemRepository.save(item2);

        List<Item> result = itemService.getAllItems();

        assertFalse(result.isEmpty());
        assertTrue(result.size() >= 2);
    }

    @Test
    void getItemById_WithExistingItem_ShouldReturnItem() {
        Item item = createItem("Test Item", "Test Description", owner, true);
        Item savedItem = itemRepository.save(item);

        Item result = itemService.getItemById(savedItem.getId());

        assertNotNull(result);
        assertEquals(savedItem.getId(), result.getId());
        assertEquals("Test Item", result.getName());
        assertEquals("Test Description", result.getDescription());
    }

    @Test
    void getItemById_WithNonExistingItem_ShouldThrowItemNotFoundException() {
        assertThrows(ItemNotFoundException.class, () -> itemService.getItemById(999L));
    }

    @Test
    void getUserItems_ShouldReturnOnlyUserItems() {
        Item item1 = createItem("Item 1", "Description 1", owner, true);
        Item item2 = createItem("Item 2", "Description 2", owner, true);
        Item item3 = createItem("Item 3", "Description 3", anotherUser, true);

        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);

        List<Item> result = itemService.getUserItems(owner.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(item -> item.getOwner().getId().equals(owner.getId())));
    }

    @Test
    void getUserItems_WithNonExistingUser_ShouldThrowUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> itemService.getUserItems(999L));
    }

    @Test
    void getItemsMatchingText_ShouldReturnMatchingAvailableItems() {
        Item item1 = createItem("Drill Machine", "Powerful electric drill", owner, true);
        Item item2 = createItem("Hammer", "Heavy construction hammer", owner, true);
        Item item3 = createItem("Broken Drill", "Doesn't work", owner, false);

        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);

        List<Item> result = itemService.getItemsMatchingText("drill");

        assertEquals(1, result.size());
        assertEquals("Drill Machine", result.get(0).getName());
        assertTrue(result.get(0).getAvailable());
    }

    @Test
    void getItemsMatchingText_WithEmptyText_ShouldReturnEmptyList() {
        Item item = createItem("Test Item", "Test Description", owner, true);
        itemRepository.save(item);

        List<Item> result = itemService.getItemsMatchingText("");

        assertTrue(result.isEmpty());
    }

    @Test
    void addItem_ShouldPersistItemWithOwner() {
        Item item = createItem("New Item", "New Description", owner, true);

        Item result = itemService.addItem(owner.getId(), item);

        assertNotNull(result.getId());
        assertEquals("New Item", result.getName());
        assertEquals("New Description", result.getDescription());
        assertTrue(result.getAvailable());
        assertEquals(owner.getId(), result.getOwner().getId());

        Item fromDb = itemRepository.findById(result.getId()).orElse(null);
        assertNotNull(fromDb);
        assertEquals(result.getId(), fromDb.getId());
    }

    @Test
    void addItem_WithNonExistingUser_ShouldThrowUserNotFoundException() {
        Item item = createItem("New Item", "New Description", owner, true);

        assertThrows(UserNotFoundException.class, () -> itemService.addItem(999L, item));
    }

    @Test
    void updateItem_ShouldUpdateExistingItem() {
        Item originalItem = createItem("Original Name", "Original Description", owner, true);
        Item savedItem = itemRepository.save(originalItem);

        Item updateData = new Item();
        updateData.setName("Updated Name");
        updateData.setDescription("Updated Description");
        updateData.setAvailable(false);

        Item result = itemService.updateItem(owner.getId(), savedItem.getId(), updateData);

        assertEquals(savedItem.getId(), result.getId());
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertFalse(result.getAvailable());
        assertEquals(owner.getId(), result.getOwner().getId());
    }

    @Test
    void updateItem_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        Item originalItem = createItem("Original Name", "Original Description", owner, true);
        Item savedItem = itemRepository.save(originalItem);

        Item updateData = new Item();
        updateData.setName("Updated Name");

        Item result = itemService.updateItem(owner.getId(), savedItem.getId(), updateData);

        assertEquals("Updated Name", result.getName());
        assertEquals("Original Description", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void updateItem_WithWrongOwner_ShouldThrowInternalValidationException() {
        Item item = createItem("Test Item", "Test Description", owner, true);
        Item savedItem = itemRepository.save(item);

        Item updateData = new Item();
        updateData.setName("Updated Name");

        assertThrows(InternalValidationException.class,
                () -> itemService.updateItem(anotherUser.getId(), savedItem.getId(), updateData));
    }

    @Test
    void deleteItem_ShouldRemoveItemFromDatabase() {
        Item item = createItem("To Delete", "Description", owner, true);
        Item savedItem = itemRepository.save(item);

        Item result = itemService.deleteItem(owner.getId(), savedItem.getId());

        assertEquals(savedItem.getId(), result.getId());
        assertFalse(itemRepository.existsById(savedItem.getId()));
    }

    @Test
    void deleteItem_WithWrongOwner_ShouldThrowInternalValidationException() {
        Item item = createItem("Test Item", "Test Description", owner, true);
        Item savedItem = itemRepository.save(item);

        assertThrows(InternalValidationException.class,
                () -> itemService.deleteItem(anotherUser.getId(), savedItem.getId()));
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
}