package ru.practicum.shareit.request.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.RequestNotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemRequestServiceImpl {

    ItemRequestRepository itemRequestRepository;

    public ItemRequest addItemRequest(ItemRequest itemRequest) {
        return itemRequestRepository.save(itemRequest);
    }

    public List<ItemRequest> getUserItemRequests(Long userId) {
        return itemRequestRepository.findByRequestor_Id(userId);
    }

    public List<ItemRequest> getAllRequests() {
        return itemRequestRepository.findAll();
    }

    public ItemRequest getItemRequest(Long requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(
                                String.format("Request with id %d not found", requestId)
                        ));
    }
}
