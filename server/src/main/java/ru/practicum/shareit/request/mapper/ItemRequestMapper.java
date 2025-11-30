package ru.practicum.shareit.request.mapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemRequestMapper {

    ItemRepository itemRepository;
    ItemMapper itemMapper;

    UserRepository userRepository;
    UserMapper userMapper;

    public ItemRequest toEntity(Long userId, ItemRequestDto itemRequestData) {
        return new ItemRequest(
                null,
                itemRequestData.getDescription(),
                userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException(
                                String.format("User with id %d not found", userId)
                        )),
                LocalDateTime.now()
        );
    }

    public ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        List<ItemDto> items = itemMapper.toItemDtoList(
                itemRepository.findByRequest_Id(itemRequest.getId()), itemRequest.getRequestor().getId()
        );

        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                userMapper.toUserDto(itemRequest.getRequestor()),
                itemRequest.getCreated(),
                items
        );
    }

    public List<ItemRequestDto> toItemRequestDtoList(List<ItemRequest> itemRequests) {
        return itemRequests.stream()
                .map(this::toItemRequestDto)
                .toList();
    }
}
