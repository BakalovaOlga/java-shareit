package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsAndBookingDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @MockBean
    private ItemMapper itemMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private Long userId;
    private Long itemId;
    private Item item;
    private User user;
    private ItemDto itemDto;
    private ItemWithCommentsAndBookingDto itemWithDetailsDto;

    @BeforeEach
    void setUp() {
        userId = 1L;
        itemId = 1L;

        user = User.builder()
                .id(userId)
                .name("Ольга")
                .email("test@mail.test")
                .build();

        itemDto = ItemDto.builder()
                .id(itemId)
                .name("Гитара")
                .description("Описание")
                .available(true)
                .ownerId(userId)
                .build();

        item = Item.builder()
                .id(itemId)
                .name("Гитара")
                .description("Описание")
                .available(true)
                .owner(user)
                .build();

        itemWithDetailsDto = ItemWithCommentsAndBookingDto.builder()
                .id(itemId)
                .name("Гитара")
                .description("Описание")
                .available(true)
                .ownerId(userId)
                .comments(List.of())
                .lastBooking(null)
                .nextBooking(null)
                .build();
    }

    // POST /bookings
    @Test
    void createItem() throws Exception {
        when(itemMapper.toEntity(any(ItemDto.class))).thenReturn(item);
        when(itemService.create(any(Item.class), eq(userId))).thenReturn(item);
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Гитара"))
                .andExpect(jsonPath("$.ownerId").value(userId));
    }

    @Test
    void createItemWithRequestId() throws Exception {
        Long requestId = 3L;
        ItemDto itemDtoWithRequest = ItemDto.builder()
                .id(itemId)
                .name("Гитара")
                .description("Описание")
                .available(true)
                .ownerId(userId)
                .requestId(requestId)
                .build();

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(requestId);

        Item itemWithRequest = Item.builder()
                .id(itemId)
                .name("Гитара")
                .description("Описание")
                .available(true)
                .owner(user)
                .request(itemRequest)
                .build();

        when(itemMapper.toEntity(any(ItemDto.class))).thenReturn(itemWithRequest);
        when(itemRequestService.getRequestEntityById(eq(requestId))).thenReturn(itemRequest);
        when(itemService.create(any(Item.class), eq(userId))).thenReturn(itemWithRequest);
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemDtoWithRequest);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDtoWithRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.requestId").value(requestId));

        verify(itemRequestService, times(1)).getRequestEntityById(requestId);
    }

    @Test
    void createItemWithoutRequestId() throws Exception {
        ItemDto itemDtoWithoutRequest = ItemDto.builder()
                .id(itemId)
                .name("Гитара")
                .description("Описание")
                .available(true)
                .ownerId(userId)
                .requestId(null)
                .build();

        Item itemWithoutRequest = Item.builder()
                .id(itemId)
                .name("Гитара")
                .description("Описание")
                .available(true)
                .owner(user)
                .request(null)
                .build();

        when(itemMapper.toEntity(any(ItemDto.class))).thenReturn(itemWithoutRequest);
        when(itemService.create(any(Item.class), eq(userId))).thenReturn(itemWithoutRequest);
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemDtoWithoutRequest);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDtoWithoutRequest)))
                .andExpect(status().isOk());

        verify(itemRequestService, never()).getRequestEntityById(anyLong());
    }

    // PATCH /items/{itemId}
    @Test
    void updateItem() throws Exception {
        ItemDto updatedItemDto = ItemDto.builder()
                .id(itemId)
                .name("Гитара акустическая")
                .description("Обновленное описание")
                .available(false)
                .ownerId(userId)
                .build();

        Item updatedItem = Item.builder()
                .id(itemId)
                .name("Гитара акустическая")
                .description("Обновленное описание")
                .available(false)
                .owner(user)
                .build();

        when(itemMapper.toEntity(any(ItemDto.class))).thenReturn(updatedItem);
        when(itemService.update(any(Item.class), eq(userId))).thenReturn(updatedItem);
        when(itemMapper.toDto(any(Item.class))).thenReturn(updatedItemDto);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedItemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Гитара акустическая"))
                .andExpect(jsonPath("$.available").value(false));
    }

    // POST /items/{itemId}/comment
    @Test
    void addComment() throws Exception {
        Long commentId = 5L;
        CommentDto commentDto = CommentDto.builder()
                .id(commentId)
                .text("Отличная гитара!")
                .authorName("Иван Иванов")
                .created(LocalDateTime.now())
                .build();

        when(itemService.addComment(eq(userId), eq(itemId), any(CommentDto.class)))
                .thenReturn(commentDto);

        CommentDto requestCommentDto = CommentDto.builder()
                .text("Отличная гитара!")
                .build();

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCommentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.text").value("Отличная гитара!"))
                .andExpect(jsonPath("$.authorName").value("Иван Иванов"));
    }

    // GET /items/{itemId}
    @Test
    void getItemById() throws Exception {
        when(itemService.getItemWithCommentsAndBookings(eq(itemId), eq(userId)))
                .thenReturn(itemWithDetailsDto);

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Гитара"))
                .andExpect(jsonPath("$.comments").isArray());
    }

    // GET /items/{itemId} без userId
    @Test
    void getItemByIdWithoutUserId() throws Exception {
        when(itemService.getItemWithCommentsAndBookings(eq(itemId), isNull()))
                .thenReturn(itemWithDetailsDto);

        mockMvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId));
    }

    // GET /items
    @Test
    void getAllItemsByOwner() throws Exception {
        List<ItemWithCommentsAndBookingDto> items = List.of(
                itemWithDetailsDto,
                ItemWithCommentsAndBookingDto.builder()
                        .id(2L)
                        .name("Молоток")
                        .description("Тяжелый молоток")
                        .available(true)
                        .ownerId(userId)
                        .comments(List.of())
                        .build()
        );

        when(itemService.getItemsWithCommentsAndBookingsByOwner(eq(userId), eq(0), eq(10)))
                .thenReturn(items);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(itemId))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    // GET /items/search
    @Test
    void searchItems() throws Exception {
        String searchText = "гитара";
        List<Item> items = List.of(item);
        List<ItemDto> itemDtos = List.of(itemDto);

        when(itemService.search(eq(searchText), eq(0), eq(10)))
                .thenReturn(items);
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemDto);

        mockMvc.perform(get("/items/search")
                        .param("text", searchText)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(itemId))
                .andExpect(jsonPath("$[0].name").value("Гитара"));
    }

    // GET /items/search с пустым текстом
    @Test
    void searchItemsWithEmptyText() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", "")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // DELETE /items/{itemId}
    @Test
    void deleteItem() throws Exception {
        mockMvc.perform(delete("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());

        verify(itemService, times(1)).delete(itemId, userId);
    }
}
