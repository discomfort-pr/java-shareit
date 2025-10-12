package ru.practicum.shareit.booking.model;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingStatus {

    Long id;

    String status;
}
