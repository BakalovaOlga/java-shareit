package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    private User user1;
    private User user2;
    private UserDto userDtoWithId;
    private UserDto userDto1WithId;
    private UserDto userDtoWithoutId;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(1L)
                .name("Olga")
                .email("test@mail.test")
                .build();

        user2 = User.builder()
                .id(2L)
                .name("Ольга")
                .email("test1@mail.test")
                .build();

        userDtoWithId = UserDto.builder()
                .id(1L)
                .name("Olga")
                .email("test@mail.test")
                .build();

        userDto1WithId = UserDto.builder()
                .id(2L)
                .name("Ольга")
                .email("test1@mail.test")
                .build();

        userDtoWithoutId = UserDto.builder()
                .name("Olga")
                .email("test@mail.test")
                .build();
    }

    // POST /users
    @Test
    void createUser() throws Exception {
        when(userMapper.toEntity(any(UserDto.class))).thenReturn(user1);
        when(userService.createUser(any(User.class))).thenReturn(user1);
        when(userMapper.toDto(any(User.class))).thenReturn(userDtoWithId);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDtoWithoutId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Olga"))
                .andExpect(jsonPath("$.email").value("test@mail.test"));
    }

    @Test
    void createUserWithBlankEmailShouldReturnBadRequest() throws Exception {
        UserDto invalidUserDto = UserDto.builder()
                .name("Name")
                .email("")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest());
    }

    // GET /users
    @Test
    void getAllUsers() throws Exception {
        List<User> users = List.of(user1, user2);

        when(userService.getAllUsers()).thenReturn(users);
        when(userMapper.toDto(user1)).thenReturn(userDtoWithId);
        when(userMapper.toDto(user2)).thenReturn(userDto1WithId);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Olga"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Ольга"));
    }

    // GET /users/{userId}
    @Test
    void getUserById() throws Exception {
        when(userService.getUser(1L)).thenReturn(user1);
        when(userMapper.toDto(user1)).thenReturn(userDtoWithId);

        mockMvc.perform(get("/users/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Olga"))
                .andExpect(jsonPath("$.email").value("test@mail.test"));
    }

    //PATCH /users/{userId}
    @Test
    void updateUser() throws Exception {
        UserDto updateDto = UserDto.builder()
                .name("Updated Olga")
                .email("updated@mail.test")
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .name("Updated Olga")
                .email("updated@mail.test")
                .build();

        UserDto updatedUserDto = UserDto.builder()
                .id(1L)
                .name("Updated Olga")
                .email("updated@mail.test")
                .build();

        when(userService.getUser(1L)).thenReturn(user1);
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(updatedUserDto);

        doNothing().when(userMapper).updateUserFromDto(updateDto, user1);

        mockMvc.perform(patch("/users/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Olga"))
                .andExpect(jsonPath("$.email").value("updated@mail.test"));
    }

    @Test
    void updateUserWithPartialData() throws Exception {
        UserDto partialUpdateDto = UserDto.builder()
                .name("Only Name Updated")
                .build();

        User partiallyUpdatedUser = User.builder()
                .id(1L)
                .name("Only Name Updated")
                .email("test@mail.test")
                .build();

        UserDto responseDto = UserDto.builder()
                .id(1L)
                .name("Only Name Updated")
                .email("test@mail.test")
                .build();

        when(userService.getUser(1L)).thenReturn(user1);
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(partiallyUpdatedUser);
        when(userMapper.toDto(partiallyUpdatedUser)).thenReturn(responseDto);

        doNothing().when(userMapper).updateUserFromDto(partialUpdateDto, user1);

        mockMvc.perform(patch("/users/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Only Name Updated"))
                .andExpect(jsonPath("$.email").value("test@mail.test"));
    }

    // DELETE /users/{userId}
    @Test
    void deleteUser() throws Exception {
        mockMvc.perform(delete("/users/{userId}", 1L))
                .andExpect(status().isNoContent());
    }
}