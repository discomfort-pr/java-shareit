package ru.practicum.shareit.request.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemRequestController {

    ItemRequestService itemRequestService;
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
        return itemRequestMapper.toItemRequestDtoList(
                itemRequestService.getUserItemRequests(userId)
        );
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return itemRequestMapper.toItemRequestDtoList(
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
