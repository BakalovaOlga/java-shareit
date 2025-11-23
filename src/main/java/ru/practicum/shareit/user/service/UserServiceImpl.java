package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User createUser(User user) {
        validateEmailForCreate(user);
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long userId, User userUpdate) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        validateEmailForUpdate(userId, userUpdate.getEmail());

        if (userUpdate.getName() != null) {
            existingUser.setName(userUpdate.getName());
        }
        if (userUpdate.getEmail() != null) {
            existingUser.setEmail(userUpdate.getEmail());
        }

        return userRepository.save(existingUser);
    }

    @Override
    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }


    private void validateEmailForCreate(User user) {
        if (user.getEmail() == null) {
            throw new ValidationException("Email обязателен для заполнения");
        }
        if (emailExists(user.getEmail())) {
            throw new ConflictException("Email уже существует: " + user.getEmail());
        }
    }

    private void validateEmailForUpdate(Long userId, String newEmail) {
        if (newEmail != null && emailExistsByOtherUser(userId, newEmail)) {
            throw new ConflictException("Email уже используется другим пользователем: " + newEmail);
        }
    }

    private boolean emailExists(String email) {
        return userRepository.findAll().stream()
                .anyMatch(u -> email.equals(u.getEmail()));
    }

    private boolean emailExistsByOtherUser(Long userId, String email) {
        return userRepository.findAll().stream()
                .anyMatch(u -> email.equals(u.getEmail()) && !u.getId().equals(userId));
    }
}
