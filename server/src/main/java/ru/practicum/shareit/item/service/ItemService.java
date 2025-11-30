package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    List<Item> getAllItems();

    Item getItemById(Long itemId);

    List<Item> getUserItems(Long userId);

    List<Item> getItemsMatchingText(String text);

    Item addItem(Long ownerId, Item itemData);

    Item updateItem(Long ownerId, Long itemId, Item itemData);

    Item deleteItem(Long ownerId, Long itemId);
}
