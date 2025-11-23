package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    User createUser(User user);

    User updateUser(Long id, User user);

    User getUser(Long id);

    List<User> getAllUsers();

    void deleteUser(Long id);
}
