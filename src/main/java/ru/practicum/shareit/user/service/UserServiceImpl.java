package ru.practicum.shareit.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;

@Transactional
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User with id %d not found", userId)
                ));
    }

    @Override
    public User addUser(User userData) {
        return userRepository.save(userData);
    }

    @Override
    public User updateUser(Long userId, User userData) {
        User updated = getUserById(userId);
        updateOfNullable(updated, userData);

        return userRepository.save(updated);
    }

    @Override
    public User deleteUser(Long userId) {
        User deleted = getUserById(userId);
        userRepository.deleteById(userId);
        return deleted;
    }

    private void updateOfNullable(User updated, User userData) {
        updated.setName(Objects.requireNonNullElse(userData.getName(), updated.getName()));
        updated.setEmail(Objects.requireNonNullElse(userData.getEmail(), updated.getEmail()));
    }
}
