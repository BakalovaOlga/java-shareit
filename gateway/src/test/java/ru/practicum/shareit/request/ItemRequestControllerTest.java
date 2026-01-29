package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemNewRequestDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestClient requestClient;

    private Long userId;
    private Long requestId;
    private ItemNewRequestDto requestDto;

    @BeforeEach
    void setUp() {
        userId = 1L;
        requestId = 10L;

        requestDto = new ItemNewRequestDto();
        requestDto.setDescription("Нужна гитара для концерта");
    }

    // POST /requests
    @Test
    void createRequest() throws Exception {
        when(requestClient.createRequest(eq(userId), any(ItemNewRequestDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnBadRequestWhenUserIdIsMissing() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRequestWithInvalidUserId() throws Exception {
        Long invalidUserId = 0L;

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", invalidUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    // GET /requests
    @Test
    void getUserRequests() throws Exception {
        int from = 0;
        int size = 10;

        String expectedResponse = "[{\"id\": 1, \"description\": \"Нужна гитара\"}]";

        when(requestClient.getUserRequests(eq(userId), eq(from), eq(size)))
                .thenReturn(ResponseEntity.ok(expectedResponse));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    void getAllRequests() throws Exception {
        int from = 0;
        int size = 10;

        String expectedResponse = String.join("",
                "[",
                "{",
                "\"id\": 100,",
                "\"description\": \"Нужна дрель\",",
                "\"userId\": 2",
                "},",
                "{",
                "\"id\": 101,",
                "\"description\": \"Нужен молоток\",",
                "\"userId\": 3",
                "}",
                "]"
        );

        when(requestClient.getAllOtherUsersRequests(userId, from, size))
                .thenReturn(ResponseEntity.ok(expectedResponse));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(2))
                .andExpect(jsonPath("$[1].userId").value(3));
    }

    // GET /requests/{requestId}
    @Test
    void getRequestById() throws Exception {
        String expectedResponse = "{\"id\": 10, \"description\": \"Нужна гитара\"}";

        when(requestClient.getRequestById(userId, requestId))
                .thenReturn(ResponseEntity.ok(expectedResponse));

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }


}
