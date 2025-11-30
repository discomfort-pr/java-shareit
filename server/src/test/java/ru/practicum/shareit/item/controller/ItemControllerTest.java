package ru.practicum.shareit.item.controller;

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
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.comment.service.CommentService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private CommentService commentService;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ItemController itemController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private ItemDto itemDto;
    private Item item;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(itemController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        itemDto = new ItemDto(
                1L,
                "Test Item",
                "Test Description",
                true,
                1L,
                null,
                null,
                null,
                null
        );

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);

        commentDto = new CommentDto(
                1L,
                "Test comment",
                "Test User",
                LocalDateTime.now()
        );
    }

    @Test
    void getItemById_ShouldReturnItem() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;

        when(itemService.getItemById(itemId)).thenReturn(item);
        when(itemMapper.toItemDto(item, null)).thenReturn(itemDto);

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void getItemById_WithoutUserIdHeader_ShouldReturnBadRequest() throws Exception {
        Long itemId = 1L;

        mockMvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserItems_ShouldReturnListOfItems() throws Exception {
        Long userId = 1L;
        List<Item> items = List.of(item);
        List<ItemDto> itemDtos = List.of(itemDto);

        when(itemService.getUserItems(userId)).thenReturn(items);
        when(itemMapper.toItemDtoList(items, userId)).thenReturn(itemDtos);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Item"))
                .andExpect(jsonPath("$[0].description").value("Test Description"));
    }

    @Test
    void getUserItems_WithoutUserIdHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItemsMatchingText_WithText_ShouldReturnMatchingItems() throws Exception {
        String searchText = "test";
        Long userId = 1L;
        List<Item> items = List.of(item);
        List<ItemDto> itemDtos = List.of(itemDto);

        when(itemService.getItemsMatchingText(searchText)).thenReturn(items);
        when(itemMapper.toItemDtoList(items, userId)).thenReturn(itemDtos);

        mockMvc.perform(get("/items/search")
                        .param("text", searchText)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Item"));
    }

    @Test
    void getItemsMatchingText_WithEmptyText_ShouldReturnEmptyList() throws Exception {
        String searchText = "";
        Long userId = 1L;
        List<Item> emptyItems = List.of();
        List<ItemDto> emptyDtos = List.of();

        when(itemService.getItemsMatchingText(searchText)).thenReturn(emptyItems);
        when(itemMapper.toItemDtoList(emptyItems, userId)).thenReturn(emptyDtos);

        mockMvc.perform(get("/items/search")
                        .param("text", searchText)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getItemsMatchingText_WithoutUserIdHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", "test"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_ShouldReturnCreatedItem() throws Exception {
        Long userId = 1L;
        ItemDto requestDto = new ItemDto(
                null,
                "New Item",
                "New Description",
                true,
                null,
                null,
                null,
                null,
                null
        );

        when(itemMapper.toEntity(any(ItemDto.class), eq(userId))).thenReturn(item);
        when(itemService.addItem(eq(userId), any(Item.class))).thenReturn(item);
        when(itemMapper.toItemDto(item, null)).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.description").value("Test Description"));
    }

    @Test
    void addItem_WithoutUserIdHeader_ShouldReturnBadRequest() throws Exception {
        ItemDto requestDto = new ItemDto(
                null,
                "New Item",
                "New Description",
                true,
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_ShouldReturnUpdatedItem() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;
        ItemDto updateDto = new ItemDto(
                null,
                "Updated Item",
                "Updated Description",
                false,
                null,
                null,
                null,
                null,
                null
        );

        Item updatedItem = new Item();
        updatedItem.setId(1L);
        updatedItem.setName("Updated Item");
        updatedItem.setDescription("Updated Description");
        updatedItem.setAvailable(false);

        ItemDto updatedItemDto = new ItemDto(
                1L,
                "Updated Item",
                "Updated Description",
                false,
                1L,
                null,
                null,
                null,
                null
        );

        when(itemMapper.toEntity(any(ItemDto.class), eq(userId))).thenReturn(updatedItem);
        when(itemService.updateItem(eq(userId), eq(itemId), any(Item.class))).thenReturn(updatedItem);
        when(itemMapper.toItemDto(updatedItem, null)).thenReturn(updatedItemDto);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Item"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void updateItem_WithoutUserIdHeader_ShouldReturnBadRequest() throws Exception {
        Long itemId = 1L;
        ItemDto updateDto = new ItemDto(
                null,
                "Updated Item",
                "Updated Description",
                false,
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteItem_ShouldReturnDeletedItem() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;

        when(itemService.deleteItem(userId, itemId)).thenReturn(item);
        when(itemMapper.toItemDto(item, null)).thenReturn(itemDto);

        mockMvc.perform(delete("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    void deleteItem_WithoutUserIdHeader_ShouldReturnBadRequest() throws Exception {
        Long itemId = 1L;

        mockMvc.perform(delete("/items/{itemId}", itemId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postComment_ShouldReturnCreatedComment() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;
        CommentDto requestComment = new CommentDto(
                null,
                "Test comment",
                null,
                null
        );

        ru.practicum.shareit.item.comment.model.Comment commentEntity =
                new ru.practicum.shareit.item.comment.model.Comment();
        commentEntity.setId(1L);
        commentEntity.setText("Test comment");

        when(commentMapper.toEntity(any(CommentDto.class), eq(itemId), eq(userId))).thenReturn(commentEntity);
        when(commentService.postComment(eq(userId), eq(itemId), any(ru.practicum.shareit.item.comment.model.Comment.class)))
                .thenReturn(commentEntity);
        when(commentMapper.toCommentDto(commentEntity, userId)).thenReturn(commentDto);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestComment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Test comment"))
                .andExpect(jsonPath("$.authorName").value("Test User"));
    }

    @Test
    void postComment_WithoutUserIdHeader_ShouldReturnBadRequest() throws Exception {
        Long itemId = 1L;
        CommentDto requestComment = new CommentDto(
                null,
                "Test comment",
                null,
                null
        );

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestComment)))
                .andExpect(status().isBadRequest());
    }
}