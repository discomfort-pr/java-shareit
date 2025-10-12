package ru.practicum.shareit.item.controller;

import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.validation.group.CreateGroup;
import ru.practicum.shareit.validation.group.UpdateGroup;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class ItemController {

    ItemService itemService;
    ItemMapper itemMapper;

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable @Positive Long itemId) {
        return itemMapper.toItemDto(
                itemService.getItemById(itemId)
        );
    }

    @GetMapping
    public List<ItemDto> getUserItems(@RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemMapper.toItemDto(
                itemService.getUserItems(userId)
        );
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsMatchingText(@RequestParam(name = "text", defaultValue = "") String text) {
        return itemMapper.toItemDto(
                itemService.getItemsMatchingText(text)
        );
    }

    @PostMapping
    public ItemDto addItem(@RequestBody @Validated(value = CreateGroup.class) ItemDto itemData,
                           @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemMapper.toItemDto(
                itemService.addItem(userId, itemMapper.toEntity(itemData))
        );
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable @Positive Long itemId,
                              @RequestBody @Validated(value = UpdateGroup.class) ItemDto itemData,
                              @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemMapper.toItemDto(
                itemService.updateItem(userId, itemId, itemMapper.toEntity(itemData))
        );
    }

    @DeleteMapping("/{itemId}")
    public ItemDto deleteItem(@PathVariable @Positive Long itemId,
                              @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemMapper.toItemDto(
                itemService.deleteItem(userId, itemId)
        );
    }
}
