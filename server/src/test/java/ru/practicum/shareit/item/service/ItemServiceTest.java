package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.InternalValidationException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void getAllItems_ShouldReturnAllItems() {
        List<Item> expectedItems = List.of(new Item(), new Item());
        when(itemRepository.findAll()).thenReturn(expectedItems);

        List<Item> result = itemService.getAllItems();

        assertEquals(expectedItems, result);
        verify(itemRepository).findAll();
    }

    @Test
    void getItemById_WithExistingItem_ShouldReturnItem() {
        Long itemId = 1L;
        Item expectedItem = new Item();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(expectedItem));

        Item result = itemService.getItemById(itemId);

        assertEquals(expectedItem, result);
        verify(itemRepository).findById(itemId);
    }

    @Test
    void getItemById_WithNonExistingItem_ShouldThrowItemNotFoundException() {
        Long itemId = 999L;
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> itemService.getItemById(itemId)
        );

        assertEquals("Item with id 999 not found", exception.getMessage());
    }

    @Test
    void getUserItems_WithExistingUser_ShouldReturnUserItems() {
        Long userId = 1L;
        List<Item> expectedItems = List.of(new Item(), new Item());

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(itemRepository.findByOwnerId(userId)).thenReturn(expectedItems);

        List<Item> result = itemService.getUserItems(userId);

        assertEquals(expectedItems, result);
        verify(userRepository).findById(userId);
        verify(itemRepository).findByOwnerId(userId);
    }

    @Test
    void getUserItems_WithNonExistingUser_ShouldThrowUserNotFoundException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> itemService.getUserItems(userId)
        );

        assertEquals("User with id 999 not found", exception.getMessage());
        verify(itemRepository, never()).findByOwnerId(anyLong());
    }

    @Test
    void getItemsMatchingText_WithNonEmptyText_ShouldReturnMatchingItems() {
        String searchText = "test";
        List<Item> expectedItems = List.of(new Item(), new Item());

        when(itemRepository.findByText(searchText)).thenReturn(expectedItems);

        List<Item> result = itemService.getItemsMatchingText(searchText);

        assertEquals(expectedItems, result);
        verify(itemRepository).findByText(searchText);
    }

    @Test
    void getItemsMatchingText_WithEmptyText_ShouldReturnEmptyList() {
        List<Item> result = itemService.getItemsMatchingText("");

        assertTrue(result.isEmpty());
        verify(itemRepository, never()).findByText(any());
    }

    @Test
    void getItemsMatchingText_WithBlankText_ShouldReturnEmptyList() {
        List<Item> result = itemService.getItemsMatchingText("   ");

        assertTrue(result.isEmpty());
        verify(itemRepository, never()).findByText(any());
    }

    @Test
    void addItem_WithValidData_ShouldSaveAndReturnItem() {
        Long ownerId = 1L;
        User owner = new User();
        owner.setId(ownerId);

        Item itemData = new Item();
        itemData.setName("Test Item");
        itemData.setOwner(owner);

        Item savedItem = new Item();
        savedItem.setId(1L);
        savedItem.setName("Test Item");

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.save(itemData)).thenReturn(savedItem);

        Item result = itemService.addItem(ownerId, itemData);

        assertEquals(savedItem, result);
        assertEquals(ownerId, itemData.getOwner().getId());
        verify(userRepository).findById(ownerId);
        verify(itemRepository).save(itemData);
    }

    @Test
    void addItem_WithNonExistingUser_ShouldThrowUserNotFoundException() {
        Long ownerId = 999L;
        Item itemData = new Item();

        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> itemService.addItem(ownerId, itemData)
        );

        assertEquals("User with id 999 not found", exception.getMessage());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItem_WithValidData_ShouldUpdateAndReturnItem() {
        Long ownerId = 1L;
        Long itemId = 1L;

        User owner = new User();
        owner.setId(ownerId);

        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setName("Old Name");
        existingItem.setDescription("Old Description");
        existingItem.setAvailable(true);
        existingItem.setOwner(owner);

        Item updateData = new Item();
        updateData.setName("New Name");
        updateData.setDescription("New Description");
        updateData.setAvailable(false);

        Item updatedItem = new Item();
        updatedItem.setId(itemId);
        updatedItem.setName("New Name");
        updatedItem.setDescription("New Description");
        updatedItem.setAvailable(false);
        updatedItem.setOwner(owner);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(existingItem)).thenReturn(updatedItem);

        Item result = itemService.updateItem(ownerId, itemId, updateData);

        assertEquals("New Name", result.getName());
        assertEquals("New Description", result.getDescription());
        assertFalse(result.getAvailable());
        verify(itemRepository).save(existingItem);
    }

    @Test
    void updateItem_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        Long ownerId = 1L;
        Long itemId = 1L;

        User owner = new User();
        owner.setId(ownerId);

        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setName("Old Name");
        existingItem.setDescription("Old Description");
        existingItem.setAvailable(true);
        existingItem.setOwner(owner);

        Item updateData = new Item();
        updateData.setName("New Name");
        updateData.setDescription(null);
        updateData.setAvailable(null);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(existingItem)).thenReturn(existingItem);

        Item result = itemService.updateItem(ownerId, itemId, updateData);

        assertEquals("New Name", result.getName());
        assertEquals("Old Description", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void updateItem_WithNonExistingUser_ShouldThrowUserNotFoundException() {
        Long ownerId = 999L;
        Long itemId = 1L;
        Item updateData = new Item();

        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> itemService.updateItem(ownerId, itemId, updateData)
        );

        assertEquals("User with id 999 not found", exception.getMessage());
        verify(itemRepository, never()).findById(anyLong());
    }

    @Test
    void updateItem_WithNonExistingItem_ShouldThrowItemNotFoundException() {
        Long ownerId = 1L;
        Long itemId = 999L;
        Item updateData = new Item();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(new User()));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> itemService.updateItem(ownerId, itemId, updateData)
        );

        assertEquals("Item with id 999 not found", exception.getMessage());
    }

    @Test
    void updateItem_WithWrongOwner_ShouldThrowInternalValidationException() {
        Long ownerId = 1L;
        Long wrongOwnerId = 2L;
        Long itemId = 1L;

        User correctOwner = new User();
        correctOwner.setId(wrongOwnerId);

        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setOwner(correctOwner);

        Item updateData = new Item();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(new User()));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));

        InternalValidationException exception = assertThrows(
                InternalValidationException.class,
                () -> itemService.updateItem(ownerId, itemId, updateData)
        );

        assertNotNull(exception);
    }

    @Test
    void deleteItem_WithValidData_ShouldDeleteAndReturnItem() {
        Long ownerId = 1L;
        Long itemId = 1L;

        User owner = new User();
        owner.setId(ownerId);

        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setOwner(owner);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));

        Item result = itemService.deleteItem(ownerId, itemId);

        assertEquals(existingItem, result);
        verify(itemRepository).deleteById(itemId);
    }

    @Test
    void deleteItem_WithNonExistingUser_ShouldThrowUserNotFoundException() {
        Long ownerId = 999L;
        Long itemId = 1L;

        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> itemService.deleteItem(ownerId, itemId)
        );

        assertEquals("User with id 999 not found", exception.getMessage());
        verify(itemRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteItem_WithWrongOwner_ShouldThrowInternalValidationException() {
        Long ownerId = 1L;
        Long wrongOwnerId = 2L;
        Long itemId = 1L;

        User correctOwner = new User();
        correctOwner.setId(wrongOwnerId);

        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setOwner(correctOwner);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(new User()));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));

        InternalValidationException exception = assertThrows(
                InternalValidationException.class,
                () -> itemService.deleteItem(ownerId, itemId)
        );

        assertNotNull(exception);
        verify(itemRepository, never()).deleteById(anyLong());
    }
}