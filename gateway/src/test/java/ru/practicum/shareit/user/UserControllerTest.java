package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    private Long userId;
    private Long userId1;
    private UserDto userDto;
    private UserDto userDto1;

    @BeforeEach
    void setUp() {
        userId = 1L;
        userId1 = 2L;

        userDto = UserDto.builder()
                .id(userId)
                .name("Olga")
                .email("test@mail.test")
                .build();

        userDto1 = UserDto.builder()
                .id(userId1)
                .name("Ольга")
                .email("test1@mail.test")
                .build();
    }

    // POST /users
    @Test
    void createUser() throws Exception {
        when(userClient.createUser(any(UserDto.class)))
                .thenReturn(ResponseEntity.ok(userDto));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());
    }

    //PATCH /users/{userId}
    @Test
    void updateUser() throws Exception {
        userDto.setName("Ola");

        when(userClient.updateUser(eq(userId), any(UserDto.class)))
                .thenReturn(ResponseEntity.ok(userDto));

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ola"));
    }

    //GET /users
    @Test
    void getAllUsers() throws Exception {
        List<UserDto> users = Arrays.asList(userDto, userDto1);

        when(userClient.getAllUsers())
                .thenReturn(ResponseEntity.ok(users));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Olga"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Ольга"));
    }

    //GET /users/{userId}
    @Test
    void getUserById() throws Exception {
        when(userClient.getUser(userId))
                .thenReturn(ResponseEntity.ok(userDto));

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Olga"));
    }

    @Test
    void getUserById_whenInvalidId_thenReturnBadRequest() throws Exception {
        Long invalidUserId = 0L;

        mockMvc.perform(get("/users/{userId}", invalidUserId))
                .andExpect(status().isBadRequest());
    }

    //DELETE /users/{userId}
    @Test
    void deleteUser() throws Exception {
        when(userClient.deleteUser(userId))
                .thenReturn(ResponseEntity.ok("User deleted"));

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted"));
    }
}




