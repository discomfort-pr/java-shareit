package ru.practicum.shareit.item.controller;

import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.comment.service.CommentService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemController {

    ItemService itemService;
    ItemMapper itemMapper;
    CommentService commentService;
    CommentMapper commentMapper;

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId,
                               @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemMapper.toItemDto(
                itemService.getItemById(itemId), null
        );
    }

    @GetMapping
    public List<ItemDto> getUserItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemMapper.toItemDtoList(
                itemService.getUserItems(userId), userId
        );
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsMatchingText(@RequestParam(name = "text", defaultValue = "") String text,
                                              @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemMapper.toItemDtoList(
                itemService.getItemsMatchingText(text), userId
        );
    }

    @PostMapping
    public ItemDto addItem(@RequestBody ItemDto itemData, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemMapper.toItemDto(
                itemService.addItem(userId, itemMapper.toEntity(itemData, userId)), null
        );
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable Long itemId,
                              @RequestBody ItemDto itemData,
                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemMapper.toItemDto(
                itemService.updateItem(userId, itemId, itemMapper.toEntity(itemData, userId)), null
        );
    }

    @DeleteMapping("/{itemId}")
    public ItemDto deleteItem(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemMapper.toItemDto(
                itemService.deleteItem(userId, itemId), null
        );
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto postComment(@PathVariable Long itemId,
                                  @RequestHeader("X-Sharer-User-Id") Long userId,
                                  @RequestBody CommentDto commentData) {
        return commentMapper.toCommentDto(
                commentService.postComment(userId, itemId, commentMapper.toEntity(commentData, itemId, userId)), userId
        );
    }
}
