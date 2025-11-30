package ru.practicum.shareit.request.controller;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestControllerTest {

    @Mock
    private ItemRequestService itemRequestService;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @InjectMocks
    private ItemRequestController itemRequestController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private ItemRequestDto itemRequestDto;
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(itemRequestController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Test User");
        userDto.setEmail("test@example.com");

        itemRequestDto = new ItemRequestDto(
                1L,
                "Need a drill for home repairs",
                userDto,
                LocalDateTime.now(),
                List.of()
        );

        itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("Need a drill for home repairs");
    }


    @Test
    void addItemRequest_WithoutUserIdHeader_ShouldReturnBadRequest() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto(null, "Need a drill", null, null, null);

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserItemRequests_ShouldReturnUserRequests() throws Exception {
        Long userId = 1L;
        List<ItemRequest> requests = List.of(itemRequest);
        List<ItemRequestDto> requestDtos = List.of(itemRequestDto);

        when(itemRequestService.getUserItemRequests(userId)).thenReturn(requests);
        when(itemRequestMapper.toItemRequestDtoList(requests)).thenReturn(requestDtos);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Need a drill for home repairs"))
                .andExpect(jsonPath("$[0].requestor.id").value(1L));
    }

    @Test
    void getUserItemRequests_WithoutUserIdHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserItemRequests_WithNoRequests_ShouldReturnEmptyList() throws Exception {
        Long userId = 1L;
        List<ItemRequest> emptyRequests = List.of();
        List<ItemRequestDto> emptyDtos = List.of();

        when(itemRequestService.getUserItemRequests(userId)).thenReturn(emptyRequests);
        when(itemRequestMapper.toItemRequestDtoList(emptyRequests)).thenReturn(emptyDtos);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getAllRequests_ShouldReturnAllRequests() throws Exception {
        Long userId = 1L;
        List<ItemRequest> requests = List.of(itemRequest);
        List<ItemRequestDto> requestDtos = List.of(itemRequestDto);

        when(itemRequestService.getAllRequests()).thenReturn(requests);
        when(itemRequestMapper.toItemRequestDtoList(requests)).thenReturn(requestDtos);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Need a drill for home repairs"));
    }

    @Test
    void getAllRequests_WithoutUserIdHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests/all"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllRequests_WithNoRequests_ShouldReturnEmptyList() throws Exception {
        Long userId = 1L;
        List<ItemRequest> emptyRequests = List.of();
        List<ItemRequestDto> emptyDtos = List.of();

        when(itemRequestService.getAllRequests()).thenReturn(emptyRequests);
        when(itemRequestMapper.toItemRequestDtoList(emptyRequests)).thenReturn(emptyDtos);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getItemRequest_ShouldReturnRequest() throws Exception {
        Long userId = 1L;
        Long requestId = 1L;

        when(itemRequestService.getItemRequest(requestId)).thenReturn(itemRequest);
        when(itemRequestMapper.toItemRequestDto(itemRequest)).thenReturn(itemRequestDto);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a drill for home repairs"))
                .andExpect(jsonPath("$.requestor.id").value(1L));
    }

    @Test
    void getItemRequest_WithoutUserIdHeader_ShouldReturnBadRequest() throws Exception {
        Long requestId = 1L;

        mockMvc.perform(get("/requests/{requestId}", requestId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItemRequest_WithMultipleRequests_ShouldReturnCorrectRequest() throws Exception {
        Long userId = 1L;
        Long requestId = 2L;

        UserDto userDto2 = new UserDto();
        userDto2.setId(2L);
        userDto2.setName("Another User");
        userDto2.setEmail("another@example.com");

        ItemRequestDto specificRequestDto = new ItemRequestDto(
                2L,
                "Need a hammer",
                userDto2,
                LocalDateTime.now(),
                List.of()
        );

        ItemRequest specificRequest = new ItemRequest();
        specificRequest.setId(2L);
        specificRequest.setDescription("Need a hammer");

        when(itemRequestService.getItemRequest(requestId)).thenReturn(specificRequest);
        when(itemRequestMapper.toItemRequestDto(specificRequest)).thenReturn(specificRequestDto);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.description").value("Need a hammer"))
                .andExpect(jsonPath("$.requestor.id").value(2L))
                .andExpect(jsonPath("$.requestor.name").value("Another User"));
    }

    @Test
    void getUserItemRequests_WithMultipleRequests_ShouldReturnAllUserRequests() throws Exception {
        Long userId = 1L;

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Test User");
        userDto.setEmail("test@example.com");

        ItemRequestDto requestDto1 = new ItemRequestDto(1L, "Need a drill", userDto, LocalDateTime.now(), List.of());
        ItemRequestDto requestDto2 = new ItemRequestDto(2L, "Need a hammer", userDto, LocalDateTime.now(), List.of());

        ItemRequest request1 = new ItemRequest();
        request1.setId(1L);
        ItemRequest request2 = new ItemRequest();
        request2.setId(2L);

        List<ItemRequest> requests = List.of(request1, request2);
        List<ItemRequestDto> requestDtos = List.of(requestDto1, requestDto2);

        when(itemRequestService.getUserItemRequests(userId)).thenReturn(requests);
        when(itemRequestMapper.toItemRequestDtoList(requests)).thenReturn(requestDtos);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Need a drill"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].description").value("Need a hammer"));
    }
}