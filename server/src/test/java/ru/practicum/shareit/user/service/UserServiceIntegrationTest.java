package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Sql(scripts = {"/test-schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserServiceIntegrationTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private ru.practicum.shareit.user.repository.UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = createUser("user1@example.com", "User One");
        user2 = createUser("user2@example.com", "User Two");
    }

    @Test
    void getAllUsers_ShouldReturnAllUsersFromDatabase() {
        userRepository.save(user1);
        userRepository.save(user2);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(user -> user.getName().equals("User One")));
        assertTrue(result.stream().anyMatch(user -> user.getName().equals("User Two")));
    }

    @Test
    void getAllUsers_WithNoUsers_ShouldReturnEmptyList() {
        List<User> result = userService.getAllUsers();

        assertTrue(result.isEmpty());
    }

    @Test
    void getUserById_WithExistingUser_ShouldReturnUser() {
        User savedUser = userRepository.save(user1);

        User result = userService.getUserById(savedUser.getId());

        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        assertEquals("User One", result.getName());
        assertEquals("user1@example.com", result.getEmail());
    }

    @Test
    void getUserById_WithNonExistingUser_ShouldThrowUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void addUser_ShouldPersistUserInDatabase() {
        User newUser = createUser("newuser@example.com", "New User");

        User result = userService.addUser(newUser);

        assertNotNull(result.getId());
        assertEquals("New User", result.getName());
        assertEquals("newuser@example.com", result.getEmail());

        User fromDb = userRepository.findById(result.getId()).orElse(null);
        assertNotNull(fromDb);
        assertEquals(result.getId(), fromDb.getId());
        assertEquals("New User", fromDb.getName());
        assertEquals("newuser@example.com", fromDb.getEmail());
    }

    @Test
    void updateUser_ShouldUpdateExistingUser() {
        User savedUser = userRepository.save(user1);

        User updateData = new User();
        updateData.setName("Updated Name");
        updateData.setEmail("updated@example.com");

        User result = userService.updateUser(savedUser.getId(), updateData);

        assertEquals(savedUser.getId(), result.getId());
        assertEquals("Updated Name", result.getName());
        assertEquals("updated@example.com", result.getEmail());

        User fromDb = userRepository.findById(savedUser.getId()).orElse(null);
        assertNotNull(fromDb);
        assertEquals("Updated Name", fromDb.getName());
        assertEquals("updated@example.com", fromDb.getEmail());
    }

    @Test
    void updateUser_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        User savedUser = userRepository.save(user1);

        // Обновляем только имя
        User updateData = new User();
        updateData.setName("Updated Name Only");

        User result = userService.updateUser(savedUser.getId(), updateData);

        assertEquals(savedUser.getId(), result.getId());
        assertEquals("Updated Name Only", result.getName());
        assertEquals("user1@example.com", result.getEmail()); // Email остался прежним

        // Обновляем только email
        User updateData2 = new User();
        updateData2.setEmail("updatedonly@example.com");

        User result2 = userService.updateUser(savedUser.getId(), updateData2);

        assertEquals("Updated Name Only", result2.getName()); // Имя осталось прежним
        assertEquals("updatedonly@example.com", result2.getEmail());
    }

    @Test
    void updateUser_WithNoChanges_ShouldReturnSameUser() {
        User savedUser = userRepository.save(user1);

        User updateData = new User(); // Пустой объект

        User result = userService.updateUser(savedUser.getId(), updateData);

        assertEquals(savedUser.getId(), result.getId());
        assertEquals("User One", result.getName());
        assertEquals("user1@example.com", result.getEmail());
    }

    @Test
    void updateUser_WithNonExistingUser_ShouldThrowUserNotFoundException() {
        User updateData = new User();
        updateData.setName("Non Existing User");

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(999L, updateData));
    }

    @Test
    void deleteUser_ShouldRemoveUserFromDatabase() {
        User savedUser = userRepository.save(user1);

        User result = userService.deleteUser(savedUser.getId());

        assertEquals(savedUser.getId(), result.getId());
        assertEquals("User One", result.getName());
        assertEquals("user1@example.com", result.getEmail());

        assertFalse(userRepository.existsById(savedUser.getId()));
    }

    @Test
    void deleteUser_WithNonExistingUser_ShouldThrowUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(999L));
    }

    @Test
    void addUser_ShouldGenerateUniqueId() {
        User user1 = createUser("test1@example.com", "Test User 1");
        User user2 = createUser("test2@example.com", "Test User 2");

        User saved1 = userService.addUser(user1);
        User saved2 = userService.addUser(user2);

        assertNotNull(saved1.getId());
        assertNotNull(saved2.getId());
        assertNotEquals(saved1.getId(), saved2.getId());
    }

    @Test
    void updateUser_WithEmptyName_ShouldUpdateWithEmptyName() {
        User savedUser = userRepository.save(user1);

        User updateData = new User();
        updateData.setName("");

        User result = userService.updateUser(savedUser.getId(), updateData);

        assertEquals("", result.getName());
        assertEquals("user1@example.com", result.getEmail());
    }

    @Test
    void updateUser_WithNullValues_ShouldNotChangeExistingValues() {
        User savedUser = userRepository.save(user1);

        User updateData = new User();
        updateData.setName(null);
        updateData.setEmail(null);

        User result = userService.updateUser(savedUser.getId(), updateData);

        assertEquals("User One", result.getName());
        assertEquals("user1@example.com", result.getEmail());
    }

    @Test
    void addUser_WithDuplicateEmail_ShouldThrowException() {
        User user1 = createUser("duplicate@example.com", "First User");
        User user2 = createUser("duplicate@example.com", "Second User");

        userService.addUser(user1);

        assertThrows(Exception.class, () -> userService.addUser(user2));
    }

    @Test
    void getAllUsers_ShouldReturnUsersInConsistentOrder() {
        User user1 = createUser("a@example.com", "User A");
        User user2 = createUser("b@example.com", "User B");
        User user3 = createUser("c@example.com", "User C");

        userService.addUser(user1);
        userService.addUser(user2);
        userService.addUser(user3);

        List<User> result = userService.getAllUsers();

        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getName().equals("User A")));
        assertTrue(result.stream().anyMatch(u -> u.getName().equals("User B")));
        assertTrue(result.stream().anyMatch(u -> u.getName().equals("User C")));
    }

    @Test
    void deleteUser_ShouldNotAffectOtherUsers() {
        User saved1 = userService.addUser(user1);
        User saved2 = userService.addUser(user2);

        userService.deleteUser(saved1.getId());

        List<User> remainingUsers = userService.getAllUsers();

        assertEquals(1, remainingUsers.size());
        assertEquals("User Two", remainingUsers.get(0).getName());
        assertEquals("user2@example.com", remainingUsers.get(0).getEmail());
    }

    private User createUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }
}