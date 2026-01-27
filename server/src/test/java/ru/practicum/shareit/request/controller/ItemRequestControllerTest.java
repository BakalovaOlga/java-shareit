package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemNewRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private Long userId;
    private Long requestId;
    private ItemNewRequestDto newRequestDto;
    private ItemRequestDto requestDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userId = 1L;
        requestId = 10L;

        userDto = UserDto.builder()
                .id(userId)
                .name("Иван Иванов")
                .email("ivan@mail.ru")
                .build();

        newRequestDto = ItemNewRequestDto.builder()
                .description("Нужна мощная дрель для ремонта")
                .build();

        requestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("Нужна мощная дрель для ремонта")
                .requestorId(userId)
                .requestor(userDto)
                .created(LocalDateTime.now())
                .items(List.of())
                .build();
    }

    // POST /requests
    @Test
    void createRequest() throws Exception {
        when(itemRequestService.createRequest(eq(userId), any(ItemNewRequestDto.class)))
                .thenReturn(requestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value("Нужна мощная дрель для ремонта"))
                .andExpect(jsonPath("$.requestorId").value(userId))
                .andExpect(jsonPath("$.requestor.id").value(userId))
                .andExpect(jsonPath("$.requestor.name").value("Иван Иванов"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    // GET /requests
    @Test
    void getOwnRequests() throws Exception {
        ItemRequestDto secondRequest = ItemRequestDto.builder()
                .id(11L)
                .description("Нужен молоток")
                .requestorId(userId)
                .requestor(userDto)
                .created(LocalDateTime.now().minusDays(1))
                .items(List.of())
                .build();

        List<ItemRequestDto> requests = List.of(requestDto, secondRequest);

        when(itemRequestService.getUserRequests(eq(userId), any(Pageable.class)))
                .thenReturn(requests);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(requestId))
                .andExpect(jsonPath("$[0].description").value("Нужна мощная дрель для ремонта"))
                .andExpect(jsonPath("$[1].id").value(11L))
                .andExpect(jsonPath("$[1].description").value("Нужен молоток"));
    }

    // GET /requests с пагинацией
    @Test
    void getOwnRequestsWithPagination() throws Exception {
        List<ItemRequestDto> requests = List.of(requestDto);

        when(itemRequestService.getUserRequests(eq(userId), any(Pageable.class)))
                .thenReturn(requests);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "5") // Пропустить первые 5
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(requestId));
    }

    // GET /requests/all
    @Test
    void getAllOtherUsersRequests() throws Exception {
        UserDto otherUserDto = UserDto.builder()
                .id(2L)
                .name("Петр Петров")
                .email("petr@mail.ru")
                .build();

        ItemRequestDto otherUserRequest = ItemRequestDto.builder()
                .id(20L)
                .description("Нужна лестница")
                .requestorId(2L)
                .requestor(otherUserDto)
                .created(LocalDateTime.now().minusHours(2))
                .items(List.of())
                .build();

        List<ItemRequestDto> requests = List.of(otherUserRequest);

        when(itemRequestService.getAllRequests(eq(userId), any(Pageable.class)))
                .thenReturn(requests);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(20L))
                .andExpect(jsonPath("$[0].description").value("Нужна лестница"))
                .andExpect(jsonPath("$[0].requestorId").value(2L))
                .andExpect(jsonPath("$[0].requestor.id").value(2L))
                .andExpect(jsonPath("$[0].requestor.name").value("Петр Петров"));
    }

    // GET /requests/{requestId}
    @Test
    void getRequestById() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Дрель ударная")
                .description("Мощная дрель с ударным механизмом")
                .available(true)
                .ownerId(3L)
                .requestId(requestId)
                .build();

        ItemRequestDto detailedRequest = ItemRequestDto.builder()
                .id(requestId)
                .description("Нужна мощная дрель для ремонта")
                .requestorId(userId)
                .requestor(userDto)
                .created(LocalDateTime.now())
                .items(List.of(itemDto))
                .build();

        when(itemRequestService.getRequestById(eq(userId), eq(requestId)))
                .thenReturn(detailedRequest);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value("Нужна мощная дрель для ремонта"))
                .andExpect(jsonPath("$.requestorId").value(userId))
                .andExpect(jsonPath("$.requestor.id").value(userId))
                .andExpect(jsonPath("$.requestor.name").value("Иван Иванов"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(1L))
                .andExpect(jsonPath("$.items[0].name").value("Дрель ударная"))
                .andExpect(jsonPath("$.items[0].description").value("Мощная дрель с ударным механизмом"))
                .andExpect(jsonPath("$.items[0].available").value(true))
                .andExpect(jsonPath("$.items[0].ownerId").value(3L))
                .andExpect(jsonPath("$.items[0].requestId").value(requestId));
    }

    // GET /requests/{requestId} без предметов
    @Test
    void getRequestByIdWithoutItems() throws Exception {
        ItemRequestDto requestWithoutItems = ItemRequestDto.builder()
                .id(requestId)
                .description("Нужна мощная дрель для ремонта")
                .requestorId(userId)
                .requestor(userDto)
                .created(LocalDateTime.now())
                .items(List.of()) // Пустой список
                .build();

        when(itemRequestService.getRequestById(eq(userId), eq(requestId)))
                .thenReturn(requestWithoutItems);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(0));
    }
}