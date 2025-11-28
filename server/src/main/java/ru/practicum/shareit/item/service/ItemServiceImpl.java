package ru.practicum.shareit.item.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.InternalValidationException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Transactional
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemServiceImpl implements ItemService {

    ItemRepository itemRepository;
    UserRepository userRepository;

    @Override
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @Override
    public Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(
                        String.format("Item with id %d not found", itemId)
                ));
    }

    @Override
    public List<Item> getUserItems(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User with id %d not found", userId)
                ));

        return itemRepository.findByOwnerId(userId);
    }

    @Override
    public List<Item> getItemsMatchingText(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        System.out.println(itemRepository.findAll());
        return itemRepository.findByText(text);
    }

    @Override
    public Item addItem(Long ownerId, Item itemData) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User with id %d not found", ownerId)
                ));

        itemData.getOwner().setId(ownerId);
        return itemRepository.save(itemData);
    }

    @Override
    public Item updateItem(Long ownerId, Long itemId, Item itemData) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User with id %d not found", ownerId)
                ));

        Item updated = getItemById(itemId);
        validateItemOwner(ownerId, updated);
        updateOfNullable(updated, itemData);

        return itemRepository.save(updated);
    }

    @Override
    public Item deleteItem(Long ownerId, Long itemId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User with id %d not found", ownerId)
                ));

        Item deleted = getItemById(itemId);
        validateItemOwner(ownerId, deleted);
        itemRepository.deleteById(itemId);

        return deleted;
    }

    private void validateItemOwner(Long ownerId, Item validated) {
        if (!Objects.equals(validated.getOwner().getId(), ownerId)) {
            throw new InternalValidationException("");
        }
    }

    private void updateOfNullable(Item updated, Item itemData) {
        updated.setName(Objects.requireNonNullElse(itemData.getName(), updated.getName()));
        updated.setDescription(Objects.requireNonNullElse(itemData.getDescription(), updated.getDescription()));
        updated.setAvailable(Objects.requireNonNullElse(itemData.getAvailable(), updated.getAvailable()));
    }
}
