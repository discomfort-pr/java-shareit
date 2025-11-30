package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        List<User> expectedUsers = List.of(
                createUser(1L, "User One", "user1@example.com"),
                createUser(2L, "User Two", "user2@example.com")
        );

        when(userRepository.findAll()).thenReturn(expectedUsers);

        List<User> result = userService.getAllUsers();

        assertEquals(expectedUsers, result);
        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_WithNoUsers_ShouldReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<User> result = userService.getAllUsers();

        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_WithExistingUser_ShouldReturnUser() {
        Long userId = 1L;
        User expectedUser = createUser(userId, "John Doe", "john@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        User result = userService.getUserById(userId);

        assertEquals(expectedUser, result);
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_WithNonExistingUser_ShouldThrowUserNotFoundException() {
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(userId)
        );

        assertEquals("User with id 999 not found", exception.getMessage());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_WithNullId_ShouldThrowUserNotFoundException() {
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(null)
        );

        assertEquals("User with id null not found", exception.getMessage());
    }

    @Test
    void addUser_WithValidUser_ShouldSaveAndReturnUser() {
        User userToSave = createUser(null, "New User", "new@example.com");
        User savedUser = createUser(1L, "New User", "new@example.com");

        when(userRepository.save(userToSave)).thenReturn(savedUser);

        User result = userService.addUser(userToSave);

        assertEquals(savedUser, result);
        verify(userRepository).save(userToSave);
    }

    @Test
    void updateUser_WithAllFields_ShouldUpdateAllFields() {
        Long userId = 1L;
        User existingUser = createUser(userId, "Old Name", "old@example.com");
        User updateData = createUser(null, "New Name", "new@example.com");
        User updatedUser = createUser(userId, "New Name", "new@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(updatedUser);

        User result = userService.updateUser(userId, updateData);

        assertEquals("New Name", result.getName());
        assertEquals("new@example.com", result.getEmail());
        verify(userRepository).findById(userId);
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUser_WithOnlyName_ShouldUpdateOnlyName() {
        Long userId = 1L;
        User existingUser = createUser(userId, "Old Name", "old@example.com");
        User updateData = createUser(null, "New Name", null);
        User updatedUser = createUser(userId, "New Name", "old@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(updatedUser);

        User result = userService.updateUser(userId, updateData);

        assertEquals("New Name", result.getName());
        assertEquals("old@example.com", result.getEmail());
    }

    @Test
    void updateUser_WithOnlyEmail_ShouldUpdateOnlyEmail() {
        Long userId = 1L;
        User existingUser = createUser(userId, "Old Name", "old@example.com");
        User updateData = createUser(null, null, "new@example.com");
        User updatedUser = createUser(userId, "Old Name", "new@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(updatedUser);

        User result = userService.updateUser(userId, updateData);

        assertEquals("Old Name", result.getName());
        assertEquals("new@example.com", result.getEmail());
    }

    @Test
    void updateUser_WithNoFields_ShouldNotChangeAnything() {
        Long userId = 1L;
        User existingUser = createUser(userId, "Original Name", "original@example.com");
        User updateData = createUser(null, null, null);
        User unchangedUser = createUser(userId, "Original Name", "original@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(unchangedUser);

        User result = userService.updateUser(userId, updateData);

        assertEquals("Original Name", result.getName());
        assertEquals("original@example.com", result.getEmail());
    }

    @Test
    void updateUser_WithEmptyName_ShouldUpdateWithEmptyName() {
        Long userId = 1L;
        User existingUser = createUser(userId, "Old Name", "old@example.com");
        User updateData = createUser(null, "", "new@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(userId, updateData);

        assertEquals("", result.getName());
        assertEquals("new@example.com", result.getEmail());
    }

    @Test
    void updateUser_WithNonExistingUser_ShouldThrowUserNotFoundException() {
        Long userId = 999L;
        User updateData = createUser(null, "New Name", "new@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUser(userId, updateData)
        );

        assertEquals("User with id 999 not found", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_WithExistingUser_ShouldDeleteAndReturnUser() {
        Long userId = 1L;
        User userToDelete = createUser(userId, "User to delete", "delete@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userToDelete));
        doNothing().when(userRepository).deleteById(userId);

        User result = userService.deleteUser(userId);

        assertEquals(userToDelete, result);
        verify(userRepository).findById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_WithNonExistingUser_ShouldThrowUserNotFoundException() {
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUser(userId)
        );

        assertEquals("User with id 999 not found", exception.getMessage());
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void updateOfNullable_ShouldUpdateNonNullValues() {
        User target = createUser(1L, "Original Name", "original@example.com");
        User source = createUser(null, "New Name", "new@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(1L, source);

        assertEquals("New Name", result.getName());
        assertEquals("new@example.com", result.getEmail());
    }

    @Test
    void addUser_WithUserHavingId_ShouldSaveWithNewId() {
        User userWithId = createUser(999L, "User with ID", "withid@example.com");
        User savedUser = createUser(1L, "User with ID", "withid@example.com");

        when(userRepository.save(userWithId)).thenReturn(savedUser);

        User result = userService.addUser(userWithId);

        assertEquals(1L, result.getId());
        verify(userRepository).save(userWithId);
    }

    private User createUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}