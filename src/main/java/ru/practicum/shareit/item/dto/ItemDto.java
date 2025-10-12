package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.validation.group.CreateGroup;

@Data
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
}
