package ru.practicum.shareit.item.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Component
public class ItemMapper {

    public ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwnerId(),
                item.getRequestId()
        );
    }

    public List<ItemDto> toItemDto(List<Item> items) {
        return items.stream()
                .map(this::toItemDto)
                .toList();
    }

    public Item toEntity(ItemDto itemData) {
        return new Item(
                itemData.getId(),
                itemData.getName(),
                itemData.getDescription(),
                itemData.getAvailable(),
                itemData.getOwnerId(),
                itemData.getRequestId()
        );
    }
}
