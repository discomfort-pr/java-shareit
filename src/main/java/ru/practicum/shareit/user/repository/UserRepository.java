package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.UniquenessViolationException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
public class UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private Long nextUserId = 0L;

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public User getUserById(Long userId) {
        if (!exists(userId)) {
            throw new UserNotFoundException("");
        }
        return users.get(userId);
    }

    public User addUser(User userData) {
        if (exists(userData.getEmail())) {
            throw new UniquenessViolationException("");
        }
        userData.setId(getNextUserId());
        users.put(userData.getId(), userData);

        return userData;
    }

    public User updateUser(Long userId, User userData) {
        User updated = getUserById(userId);

        if (userData.getEmail() != null && exists(userData.getEmail())) {
            throw new UniquenessViolationException("");
        }
        updateOfNullable(updated, userData);
        users.put(userId, updated);

        return updated;
    }

    public User deleteUser(Long userId) {
        User deleted = getUserById(userId);
        users.remove(userId);

        return deleted;
    }

    private boolean exists(Long userId) {
        return users.containsKey(userId);
    }

    private boolean exists(String email) {
        return users.values().stream()
                .map(User::getEmail)
                .filter(Objects::nonNull)
                .anyMatch(address -> address.equals(email));
    }

    private Long getNextUserId() {
        return ++nextUserId;
    }

    private void updateOfNullable(User updated, User userData) {
        updated.setName(Objects.requireNonNullElse(userData.getName(), updated.getName()));
        updated.setEmail(Objects.requireNonNullElse(userData.getEmail(), updated.getEmail()));
    }
}
