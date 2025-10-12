package ru.practicum.shareit.item.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemServiceImpl implements ItemService {

    ItemRepository itemRepository;
    UserRepository userRepository;

    @Override
    public List<Item> getAllItems() {
        return itemRepository.getAllItems();
    }

    @Override
    public Item getItemById(Long itemId) {
        return itemRepository.getItemById(itemId);
    }

    @Override
    public List<Item> getUserItems(Long userId) {
        userRepository.getUserById(userId);

        return itemRepository.getUserItems(userId);
    }

    @Override
    public List<Item> getItemsMatchingText(String text) {
        return itemRepository.getItemsMatchingText(text);
    }

    @Override
    public Item addItem(Long ownerId, Item itemData) {
        userRepository.getUserById(ownerId);

        return itemRepository.addItem(ownerId, itemData);
    }

    @Override
    public Item updateItem(Long ownerId, Long itemId, Item itemData) {
        userRepository.getUserById(ownerId);

        return itemRepository.updateItem(ownerId, itemId, itemData);
    }

    @Override
    public Item deleteItem(Long ownerId, Long itemId) {
        userRepository.getUserById(ownerId);

        return itemRepository.deleteItem(ownerId, itemId);
    }
}
