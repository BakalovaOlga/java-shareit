package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    private final Long userId = 1L;
    private final Long itemId = 1L;
    private final ItemDto itemDto = ItemDto.builder()
            .id(itemId)
            .name("Item")
            .description("Description")
            .available(true)
            .build();

    private final CommentDto commentDto = CommentDto.builder()
            .id(1L)
            .text("Comment")
            .authorName("Author")
            .created(LocalDateTime.now())
            .build();

    @Test
    void createItem_shouldReturnOk() throws Exception {
        when(itemClient.createItem(eq(userId), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok(itemDto));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    @Test
    void createItem_withoutUserId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItem_withInvalidItem_shouldReturnBadRequest() throws Exception {
        ItemDto invalidItem = ItemDto.builder()
                .name("")
                .description("")
                .available(null)
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItem)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_shouldReturnOk() throws Exception {
        when(itemClient.updateItem(eq(userId), eq(itemId), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok(itemDto));

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateItem_withInvalidPath_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/items/{itemId}", 0)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_shouldReturnOk() throws Exception {
        when(itemClient.addComment(eq(userId), eq(itemId), any(CommentDto.class)))
                .thenReturn(ResponseEntity.ok(commentDto));

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());
    }

    @Test
    void getItemById_shouldReturnOk() throws Exception {
        when(itemClient.getItemById(eq(userId), eq(itemId)))
                .thenReturn(ResponseEntity.ok(itemDto));

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }

    @Test
    void getItemById_withoutUserId_shouldReturnOk() throws Exception {
        when(itemClient.getItemById(eq(0L), eq(itemId)))
                .thenReturn(ResponseEntity.ok(itemDto));

        mockMvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isOk());
    }

    @Test
    void getAllItemsByOwner_shouldReturnOk() throws Exception {
        when(itemClient.getItemsByOwner(eq(userId), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok("[]"));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllItemsByOwner_withoutParams_shouldUseDefaults() throws Exception {
        when(itemClient.getItemsByOwner(eq(userId), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok("[]"));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems_shouldReturnOk() throws Exception {
        when(itemClient.searchItems(anyString(), anyInt(), anyInt(), anyLong()))
                .thenReturn(ResponseEntity.ok("[]"));

        mockMvc.perform(get("/items/search")
                        .param("text", "item")
                        .param("from", "0")
                        .param("size", "10")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems_withBlankText_shouldReturnEmptyArray() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", " ")
                        .param("from", "0")
                        .param("size", "10")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void searchItems_withoutUserId_shouldReturnOk() throws Exception {
        when(itemClient.searchItems(anyString(), anyInt(), anyInt(), eq(null)))
                .thenReturn(ResponseEntity.ok("[]"));

        mockMvc.perform(get("/items/search")
                        .param("text", "item")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteItem_shouldReturnOk() throws Exception {
        when(itemClient.deleteItem(eq(userId), eq(itemId)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }

    @Test
    void deleteItem_withInvalidUserId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", 0))
                .andExpect(status().isBadRequest());
    }
}