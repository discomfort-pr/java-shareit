package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.InternalValidationException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
public class ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();
    private Long nextItemId = 0L;

    public List<Item> getAllItems() {
        return new ArrayList<>(items.values());
    }

    public Item getItemById(Long itemId) {
        if (!exists(itemId)) {
            throw new ItemNotFoundException("");
        }
        return items.get(itemId);
    }

    public List<Item> getUserItems(Long userId) {
        return items.values().stream()
                .filter(item -> Objects.equals(item.getOwnerId(), userId))
                .toList();
    }

    public List<Item> getItemsMatchingText(String text) {
        return text.isBlank()
                ? new ArrayList<>()
                : items.values().stream()
                .filter(
                        item -> item.getName().toUpperCase().contains(text.toUpperCase()) ||
                                item.getDescription().toUpperCase().contains(text.toUpperCase())
                )
                .filter(Item::getAvailable)
                .toList();
    }

    public Item addItem(Long ownerId, Item itemData) {
        itemData.setId(getNextItemId());
        itemData.setOwnerId(ownerId);
        items.put(itemData.getId(), itemData);

        return itemData;
    }

    public Item updateItem(Long ownerId, Long itemId, Item itemData) {
        Item updated = getItemById(itemId);

        validateItemOwner(ownerId, updated);
        updateOfNullable(updated, itemData);
        items.put(itemId, updated);

        return updated;
    }

    public Item deleteItem(Long ownerId, Long itemId) {
        Item deleted = getItemById(itemId);

        validateItemOwner(ownerId, deleted);
        items.remove(itemId);

        return deleted;
    }

    private boolean exists(Long itemId) {
        return items.containsKey(itemId);
    }

    private Long getNextItemId() {
        return ++nextItemId;
    }

    private void validateItemOwner(Long ownerId, Item validated) {
        if (!Objects.equals(validated.getOwnerId(), ownerId)) {
            throw new InternalValidationException("");
        }
    }

    private void updateOfNullable(Item updated, Item itemData) {
        updated.setName(Objects.requireNonNullElse(itemData.getName(), updated.getName()));
        updated.setDescription(Objects.requireNonNullElse(itemData.getDescription(), updated.getDescription()));
        updated.setAvailable(Objects.requireNonNullElse(itemData.getAvailable(), updated.getAvailable()));
        if (itemData.getRequestId() != null) {
            updated.setRequestId(itemData.getRequestId());
        }
    }
}
