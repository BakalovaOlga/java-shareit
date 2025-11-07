package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            return create(user);
        } else {
            return update(user);
        }
    }

    private User create(User newUser) {
        newUser.setId(nextId++);
        users.put(newUser.getId(), newUser);
        return newUser;
    }

    private User update(User user) {
        User existingUser = users.get(user.getId());

        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }

        return existingUser;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void deleteById(Long id) {
        users.remove(id);
    }
}
