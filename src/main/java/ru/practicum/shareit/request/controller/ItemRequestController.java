package ru.practicum.shareit.request.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class ItemRequestController {

    ItemRequestServiceImpl itemRequestService;
    ItemRequestMapper itemRequestMapper;

    @PostMapping
    public ItemRequestDto addItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestMapper.toItemRequestDto(
                itemRequestService.addItemRequest(itemRequestMapper.toEntity(userId, itemRequestDto))
        );
    }

    @GetMapping
    public List<ItemRequestDto> getUserItemRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestMapper.toItemRequestDto(
                itemRequestService.getUserItemRequests(userId)
        );
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return itemRequestMapper.toItemRequestDto(
                itemRequestService.getAllRequests()
        );
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequest(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                         @PathVariable Long requestId) {
        return itemRequestMapper.toItemRequestDto(
                itemRequestService.getItemRequest(requestId)
        );
    }
}
