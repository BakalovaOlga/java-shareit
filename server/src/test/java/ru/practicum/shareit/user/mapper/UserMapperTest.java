package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserMapperTest {
    @Autowired
    private UserMapper userMapper;


    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Ольга")
                .email("test@mail.test")
                .build();

        userDto = UserDto.builder()
                .id(2L)
                .name("Olga")
                .email("test1@mail.test")
                .build();
    }

    @Test
    void toDto() {
        UserDto result = userMapper.toDto(user);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void toDtoWithNullUserShouldReturnNull() {
        UserDto result = userMapper.toDto(null);

        assertNull(result);
    }

    @Test
    void toEntity() {
        User result = userMapper.toEntity(userDto);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getEmail(), result.getEmail());
    }

    @Test
    void updateUserFromDtoShouldUpdateNonNullFields() {
        User existingUser = user;

        UserDto updateDto = UserDto.builder()
                .id(999L)
                .name("Новое имя")
                .email("new.email@example.com")
                .build();

        userMapper.updateUserFromDto(updateDto, existingUser);

        assertEquals(1L, existingUser.getId());
        assertEquals("Новое имя", existingUser.getName());
        assertEquals("new.email@example.com", existingUser.getEmail());
    }

    @Test
    void toDtoList() {
        User user1 = user;

        User user2 = User.builder()
                .id(2L)
                .name("User 2")
                .email("user2@example.com")
                .build();

        List<User> users = List.of(user1, user2);

        List<UserDto> result = userMapper.toDtoList(users);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(user1.getId(), result.get(0).getId());
        assertEquals(user2.getId(), result.get(1).getId());
        assertEquals(user1.getName(), result.get(0).getName());
        assertEquals(user2.getName(), result.get(1).getName());
    }
}
