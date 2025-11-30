package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingCategory;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.ShortUserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.item.dto.ShortItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingController bookingController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private BookingDtoIn bookingDtoIn;
    private BookingDtoOut bookingDtoOut;
    private Booking booking;
    private User user;
    private Item item;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookingController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(user);

        bookingDtoIn = new BookingDtoIn(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        bookingDtoOut = new BookingDtoOut(
                1L,
                bookingDtoIn.getStart(),
                bookingDtoIn.getEnd(),
                new ShortItemDto(1L, "Test Item"),
                new ShortUserDto(1L, "Test user"),
                BookingStatus.WAITING
        );

        booking = new Booking();
        booking.setId(1L);
        booking.setStart(bookingDtoIn.getStart());
        booking.setEnd(bookingDtoIn.getEnd());
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.WAITING);
    }

    @Test
    void addBooking_ShouldReturnBookingDtoOut() throws Exception {
        when(bookingMapper.toEntity(any(BookingDtoIn.class), anyLong())).thenReturn(booking);
        when(bookingService.addBooking(anyLong(), any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toBookingDtoOut(any(Booking.class))).thenReturn(bookingDtoOut);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDtoIn)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.item.id").value(1L))
                .andExpect(jsonPath("$.item.name").value("Test Item"))
                .andExpect(jsonPath("$.booker.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void processBooking_ShouldReturnUpdatedBooking() throws Exception {
        BookingDtoOut updatedBookingDtoOut = new BookingDtoOut(
                1L,
                bookingDtoIn.getStart(),
                bookingDtoIn.getEnd(),
                new ShortItemDto(1L, "Test Item"),
                new ShortUserDto(1L, "test user"),
                BookingStatus.APPROVED
        );

        Booking updatedBooking = new Booking();
        updatedBooking.setId(1L);
        updatedBooking.setStatus(BookingStatus.APPROVED);

        when(bookingService.processBooking(anyLong(), anyLong(), anyString())).thenReturn(updatedBooking);
        when(bookingMapper.toBookingDtoOut(any(Booking.class))).thenReturn(updatedBookingDtoOut);

        mockMvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBooking_ShouldReturnBooking() throws Exception {
        when(bookingService.getBooking(anyLong(), anyLong())).thenReturn(booking);
        when(bookingMapper.toBookingDtoOut(any(Booking.class))).thenReturn(bookingDtoOut);

        mockMvc.perform(get("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.item.id").value(1L))
                .andExpect(jsonPath("$.booker.id").value(1L));
    }

    @Test
    void getUserBookings_ShouldReturnListOfBookings() throws Exception {
        List<Booking> bookings = List.of(booking);
        List<BookingDtoOut> bookingDtoOuts = List.of(bookingDtoOut);

        when(bookingService.getUserBookings(anyLong(), any(BookingCategory.class))).thenReturn(bookings);
        when(bookingMapper.toBookingDtoOutList(any(List.class))).thenReturn(bookingDtoOuts);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("category", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].item.id").value(1L))
                .andExpect(jsonPath("$[0].booker.id").value(1L));
    }

    @Test
    void getUserBookings_WithDefaultCategory_ShouldReturnListOfBookings() throws Exception {
        List<Booking> bookings = List.of(booking);
        List<BookingDtoOut> bookingDtoOuts = List.of(bookingDtoOut);

        when(bookingService.getUserBookings(anyLong(), any(BookingCategory.class))).thenReturn(bookings);
        when(bookingMapper.toBookingDtoOutList(any(List.class))).thenReturn(bookingDtoOuts);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getUserItemsBookings_ShouldReturnListOfBookings() throws Exception {
        List<Booking> bookings = List.of(booking);
        List<BookingDtoOut> bookingDtoOuts = List.of(bookingDtoOut);

        when(bookingService.getUserItemsBookings(anyLong(), any(BookingCategory.class))).thenReturn(bookings);
        when(bookingMapper.toBookingDtoOutList(any(List.class))).thenReturn(bookingDtoOuts);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("category", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].item.id").value(1L))
                .andExpect(jsonPath("$[0].booker.id").value(1L));
    }

    @Test
    void getUserItemsBookings_WithDefaultCategory_ShouldReturnListOfBookings() throws Exception {
        List<Booking> bookings = List.of(booking);
        List<BookingDtoOut> bookingDtoOuts = List.of(bookingDtoOut);

        when(bookingService.getUserItemsBookings(anyLong(), any(BookingCategory.class))).thenReturn(bookings);
        when(bookingMapper.toBookingDtoOutList(any(List.class))).thenReturn(bookingDtoOuts);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void addBooking_WithoutUserIdHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDtoIn)))
                .andExpect(status().isBadRequest());
    }
}