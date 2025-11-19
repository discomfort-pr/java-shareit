package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemMapper {

    CommentRepository commentRepository;
    CommentMapper commentMapper;

    BookingRepository bookingRepository;

    public ItemDto toItemDto(Item item, Long userId) {
        List<Booking> lastBookingCandidates = bookingRepository.findByEndBefore(LocalDateTime.now())
                .stream()
                .sorted(Comparator.comparing(Booking::getEnd))
                .toList().reversed();

        List<Booking> nextBookingCandidates = bookingRepository.findByStartAfter(LocalDateTime.now())
                .stream()
                .sorted(Comparator.comparing(Booking::getEnd))
                .toList();

        Booking lastBooking = (lastBookingCandidates.isEmpty()) ? null : lastBookingCandidates.getFirst();
        Booking nextBooking = (nextBookingCandidates.isEmpty()) ? null : nextBookingCandidates.getFirst();

        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwnerId(),
                item.getRequestId(),
                (Objects.equals(item.getOwnerId(), userId)) ? lastBooking : null,
                (Objects.equals(item.getOwnerId(), userId)) ? nextBooking : null,
                commentMapper.toCommentDto(commentRepository.findByItemId(item.getId()))
        );
    }

    public List<ItemDto> toItemDto(List<Item> items, Long userId) {
        return items.stream()
                .map(item -> toItemDto(item, userId))
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
