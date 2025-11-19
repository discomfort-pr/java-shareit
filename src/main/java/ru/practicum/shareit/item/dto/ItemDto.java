package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.validation.group.CreateGroup;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDto {

    Long id;

    @NotBlank(groups = CreateGroup.class)
    String name;

    @NotBlank(groups = CreateGroup.class)
    String description;

    @NotNull(groups = CreateGroup.class)
    Boolean available;

    Long ownerId;

    Long requestId;

    Booking lastBooking;

    Booking nextBooking;

    List<CommentDto> comments;
}
