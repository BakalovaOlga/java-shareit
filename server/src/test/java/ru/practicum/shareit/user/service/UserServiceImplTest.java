package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImplTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setName("Имя");
        testUser.setEmail("test@mail.test");
        userRepository.save(testUser);

        User user1 = new User();
        user1.setName("Ольга");
        user1.setEmail("test1@mail.test");
        userRepository.save(user1);

        User user2 = new User();
        user2.setName("Olga");
        user2.setEmail("test2@mail.test");
        userRepository.save(user2);
    }

    @Test
    void createUserShouldCreateNewUser() {
        User newUser = new User();
        newUser.setName("Вася");
        newUser.setEmail("newuser@mail.test");

        User createdUser = userService.createUser(newUser);

        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("Вася", createdUser.getName());
        assertEquals("newuser@mail.test", createdUser.getEmail());

        User savedUser = userRepository.findById(createdUser.getId()).orElse(null);
        assertNotNull(savedUser);
        assertEquals("Вася", savedUser.getName());
    }

    @Test
    void createUserShouldThrowConflictExceptionWhenEmailAlreadyExists() {
        User newUser = new User();
        newUser.setName("новый пользователь");
        newUser.setEmail("test@mail.test");

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.createUser(newUser));
        assertEquals("Email уже существует: test@mail.test", exception.getMessage());
    }

    @Test
    void createUserWithValidUniqueEmailShouldWork() {
        User newUser = new User();
        newUser.setName("Уникальный Пользователь");
        newUser.setEmail("unique@mail.test");

        User createdUser = userService.createUser(newUser);

        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("Уникальный Пользователь", createdUser.getName());
        assertEquals("unique@mail.test", createdUser.getEmail());
    }

    @Test
    void createUserWhenEmailDoesNotExistShouldCreateUser() {
        User newUser = new User();
        newUser.setName("Новый");
        newUser.setEmail("new@mail.test");

        User created = userService.createUser(newUser);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("new@mail.test", created.getEmail());
    }

    @Test
    void updateUserShouldUpdateUser() {
        User updateData = new User();
        updateData.setName("Новое Имя");

        User updatedUser = userService.updateUser(testUser.getId(), updateData);

        assertNotNull(updatedUser);
        assertEquals(testUser.getId(), updatedUser.getId());
        assertEquals("Новое Имя", updatedUser.getName());
        assertEquals("test@mail.test", updatedUser.getEmail());
    }

    @Test
    void updateUserShouldThrowConflictExceptionWhenEmailUsedByOtherUser() {
        User anotherUser = new User();
        anotherUser.setName("новый пользователь");
        anotherUser.setEmail("another@mail.test");
        userRepository.save(anotherUser);

        User updateData = new User();
        updateData.setEmail("another@mail.test");

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.updateUser(testUser.getId(), updateData));
        assertEquals("Email уже используется другим пользователем: another@mail.test", exception.getMessage());
    }

    @Test
    void updateUserWithNullEmailShouldUpdateNameOnly() {
        User updateData = new User();
        updateData.setName("Новое Имя");
        updateData.setEmail(null);

        User updatedUser = userService.updateUser(testUser.getId(), updateData);

        assertNotNull(updatedUser);
        assertEquals(testUser.getId(), updatedUser.getId());
        assertEquals("Новое Имя", updatedUser.getName());
        assertEquals("test@mail.test", updatedUser.getEmail());
    }

    @Test
    void updateUserWithNullNameAndEmailShouldNotChangeAnything() {
        User updateData = new User();
        updateData.setName(null);
        updateData.setEmail(null);

        User updatedUser = userService.updateUser(testUser.getId(), updateData);

        assertNotNull(updatedUser);
        assertEquals(testUser.getId(), updatedUser.getId());
        assertEquals("Имя", updatedUser.getName());
        assertEquals("test@mail.test", updatedUser.getEmail());
    }

    @Test
    void updateUserWithSameEmailShouldWork() {
        User updateData = new User();
        updateData.setEmail("test@mail.test");

        User updatedUser = userService.updateUser(testUser.getId(), updateData);

        assertNotNull(updatedUser);
        assertEquals("test@mail.test", updatedUser.getEmail());
    }

    @Test
    void updateUserWhenUserNotFoundShouldThrowNotFoundException() {
        Long nonExistentId = 999L;
        User updateData = new User();
        updateData.setName("Новое имя");

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.updateUser(nonExistentId, updateData));

        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void updateUserWhenNewEmailNotNullAndNotUsedByOthersShouldUpdate() {
        User updateData = new User();
        updateData.setEmail("newunique@mail.test");

        User updated = userService.updateUser(testUser.getId(), updateData);

        assertEquals("newunique@mail.test", updated.getEmail());
    }

    @Test
    void updateUserWhenNewEmailNotNullAndUsedByOthersShouldThrowConflictException() {
        User otherUser = new User();
        otherUser.setName("Другой");
        otherUser.setEmail("other@mail.test");
        userRepository.save(otherUser);

        User updateData = new User();
        updateData.setEmail("other@mail.test");

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.updateUser(testUser.getId(), updateData));

        assertEquals("Email уже используется другим пользователем: other@mail.test", exception.getMessage());
    }

    @Test
    void updateUserWhenNameNullEmailNotNullShouldUpdateEmailOnly() {
        User updateData = new User();
        updateData.setName(null);
        updateData.setEmail("onlyemail@mail.test");

        User updated = userService.updateUser(testUser.getId(), updateData);

        assertEquals("Имя", updated.getName());
        assertEquals("onlyemail@mail.test", updated.getEmail());
    }

    @Test
    void updateUserWhenNameNotNullEmailNullShouldUpdateNameOnly() {
        User updateData = new User();
        updateData.setName("Только имя");
        updateData.setEmail(null);

        User updated = userService.updateUser(testUser.getId(), updateData);

        assertEquals("Только имя", updated.getName());
        assertEquals("test@mail.test", updated.getEmail());
    }

    @Test
    void getAllUsersShouldReturnAllUsers() {
        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(3, result.size());

        User foundUser1 = result.stream()
                .filter(u -> u.getEmail().equals("test1@mail.test"))
                .findFirst()
                .orElse(null);

        assertNotNull(foundUser1);
        assertEquals("Ольга", foundUser1.getName());
        assertNotNull(foundUser1.getId());

        User foundUser2 = result.stream()
                .filter(u -> u.getEmail().equals("test2@mail.test"))
                .findFirst()
                .orElse(null);

        assertNotNull(foundUser2);
        assertEquals("Olga", foundUser2.getName());
        assertNotNull(foundUser2.getId());
    }

    @Test
    void deleteUserShouldDeleteUser() {
        Long userId = testUser.getId();

        userService.deleteUser(userId);

        assertFalse(userRepository.existsById(userId));
        assertEquals(2, userRepository.findAll().size());
    }

    @Test
    void deleteUserWhenUserNotFoundShouldThrowException() {
        Long nonExistentId = 999L;

        try {
            userService.deleteUser(nonExistentId);
            assertFalse(userRepository.existsById(nonExistentId));
        } catch (Exception e) {
            assertTrue(e instanceof NotFoundException ||
                    e.getMessage().contains("не найден"));
        }
    }

    @Test
    void getUserWhenUserNotFoundShouldThrowNotFoundException() {
        Long nonExistentId = 999L;

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getUser(nonExistentId));

        assertEquals("Пользователь не найден", exception.getMessage());
    }
}