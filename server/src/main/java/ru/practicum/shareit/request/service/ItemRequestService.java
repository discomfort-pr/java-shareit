package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestService {
    ItemRequest addItemRequest(ItemRequest itemRequest);

    List<ItemRequest> getUserItemRequests(Long userId);

    List<ItemRequest> getAllRequests();

    ItemRequest getItemRequest(Long requestId);
}
