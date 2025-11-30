package ru.practicum.shareit.item;

import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.validation.group.CreateGroup;
import ru.practicum.shareit.validation.group.UpdateGroup;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class ItemController {

    ItemClient itemClient;

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable @Positive Long itemId,
                                      @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemClient.get(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserItems(@RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemClient.get(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getItemsMatchingText(@RequestParam(name = "text", defaultValue = "") String text,
                                                       @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemClient.get(text);
    }

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestBody @Validated(value = CreateGroup.class) ItemDto itemData,
                           @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemClient.post(userId, itemData);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@PathVariable @Positive Long itemId,
                              @RequestBody @Validated(value = UpdateGroup.class) ItemDto itemData,
                              @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemClient.patch(itemId, userId, itemData);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(@PathVariable @Positive Long itemId,
                              @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemClient.delete(itemId, userId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> postComment(@PathVariable @Positive Long itemId,
                                  @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                  @RequestBody CommentDto commentData) {
        return itemClient.post(itemId, userId, commentData);
    }
}
