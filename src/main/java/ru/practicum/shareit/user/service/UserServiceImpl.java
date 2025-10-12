package ru.practicum.shareit.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.getUserById(userId);
    }

    @Override
    public User addUser(User userData) {
        return userRepository.addUser(userData);
    }

    @Override
    public User updateUser(Long userId, User userData) {
        return userRepository.updateUser(userId, userData);
    }

    @Override
    public User deleteUser(Long userId) {
        return userRepository.deleteUser(userId);
    }
}
