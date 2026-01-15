package ru.practicum.shareit.user.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(scripts = "/test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        user1 = createUser("user1@example.com", "User One");
        user2 = createUser("user2@example.com", "User Two");
        user3 = createUser("user3@example.com", "User Three");
    }

    @Test
    void save_ShouldPersistUserWithAllFields() {
        User newUser = createUser("newuser@example.com", "New User");

        User savedUser = userRepository.save(newUser);

        assertNotNull(savedUser.getId());
        assertEquals("New User", savedUser.getName());
        assertEquals("newuser@example.com", savedUser.getEmail());

        User retrieved = userRepository.findById(savedUser.getId()).orElse(null);
        assertNotNull(retrieved);
        assertEquals(savedUser.getId(), retrieved.getId());
        assertEquals("New User", retrieved.getName());
        assertEquals("newuser@example.com", retrieved.getEmail());
    }

    @Test
    void findById_WithExistingUser_ShouldReturnUser() {
        entityManager.persist(user1);
        entityManager.flush();

        Optional<User> result = userRepository.findById(user1.getId());

        assertTrue(result.isPresent());
        assertEquals(user1.getId(), result.get().getId());
        assertEquals("User One", result.get().getName());
        assertEquals("user1@example.com", result.get().getEmail());
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        Optional<User> result = userRepository.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);
        entityManager.flush();

        List<User> result = userRepository.findAll();

        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(user -> user.getName().equals("User One")));
        assertTrue(result.stream().anyMatch(user -> user.getName().equals("User Two")));
        assertTrue(result.stream().anyMatch(user -> user.getName().equals("User Three")));
    }

    @Test
    void findAll_WithNoUsers_ShouldReturnEmptyList() {
        List<User> result = userRepository.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteById_ShouldRemoveUser() {
        entityManager.persist(user1);
        entityManager.flush();

        Long userId = user1.getId();
        userRepository.deleteById(userId);
        entityManager.flush();

        Optional<User> result = userRepository.findById(userId);
        assertFalse(result.isPresent());
    }

    @Test
    void updateUser_ShouldModifyExistingUser() {
        entityManager.persist(user1);
        entityManager.flush();

        user1.setName("Updated Name");
        user1.setEmail("updated@example.com");
        User updatedUser = userRepository.save(user1);

        assertEquals(user1.getId(), updatedUser.getId());
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@example.com", updatedUser.getEmail());

        User fromDb = userRepository.findById(user1.getId()).orElse(null);
        assertNotNull(fromDb);
        assertEquals("Updated Name", fromDb.getName());
        assertEquals("updated@example.com", fromDb.getEmail());
    }

    @Test
    void save_WithDuplicateEmail_ShouldThrowException() {
        User user1 = createUser("duplicate@example.com", "First User");
        User user2 = createUser("duplicate@example.com", "Second User");

        entityManager.persist(user1);
        entityManager.flush();

        assertThrows(Exception.class, () -> {
            entityManager.persist(user2);
            entityManager.flush();
        });
    }

    @Test
    void save_WithNullEmail_ShouldThrowException() {
        User user = new User();
        user.setName("User Without Email");
        user.setEmail(null);

        assertThrows(Exception.class, () -> {
            entityManager.persist(user);
            entityManager.flush();
        });
    }

    @Test
    void save_WithEmptyName_ShouldWork() {
        User user = new User();
        user.setName("");
        user.setEmail("emptyname@example.com");

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("", savedUser.getName());
        assertEquals("emptyname@example.com", savedUser.getEmail());
    }

    @Test
    void save_WithEmptyEmail_ShouldThrowException() {
        User user = new User();
        user.setName("User With Empty Email");

        assertThrows(Exception.class, () -> {
            entityManager.persist(user);
            entityManager.flush();
        });
    }

    @Test
    void existsById_WithExistingUser_ShouldReturnTrue() {
        entityManager.persist(user1);
        entityManager.flush();

        boolean exists = userRepository.existsById(user1.getId());

        assertTrue(exists);
    }

    @Test
    void existsById_WithNonExistingUser_ShouldReturnFalse() {
        boolean exists = userRepository.existsById(999L);

        assertFalse(exists);
    }

    @Test
    void count_ShouldReturnCorrectNumberOfUsers() {
        assertEquals(0, userRepository.count());

        entityManager.persist(user1);
        entityManager.flush();
        assertEquals(1, userRepository.count());

        entityManager.persist(user2);
        entityManager.flush();
        assertEquals(2, userRepository.count());
    }

    @Test
    void deleteAll_ShouldRemoveAllUsers() {
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);
        entityManager.flush();

        assertEquals(3, userRepository.count());

        userRepository.deleteAll();
        entityManager.flush();

        assertEquals(0, userRepository.count());
        assertTrue(userRepository.findAll().isEmpty());
    }

    @Test
    void save_WithSpecialCharactersInName_ShouldWorkCorrectly() {
        User user = new User();
        user.setName("User with spéciäl chàràctêrs");
        user.setEmail("special@example.com");

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("User with spéciäl chàràctêrs", savedUser.getName());
        assertEquals("special@example.com", savedUser.getEmail());

        User retrieved = userRepository.findById(savedUser.getId()).orElse(null);
        assertNotNull(retrieved);
        assertEquals("User with spéciäl chàràctêrs", retrieved.getName());
    }

    @Test
    void save_WithLongName_ShouldWorkCorrectly() {
        String longName = "A".repeat(255);
        User user = new User();
        user.setName(longName);
        user.setEmail("longname@example.com");

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals(longName, savedUser.getName());
        assertEquals("longname@example.com", savedUser.getEmail());
    }

    @Test
    void save_WithComplexEmail_ShouldWorkCorrectly() {
        User user = new User();
        user.setName("Complex Email User");
        user.setEmail("user.name+tag@subdomain.example.co.uk");

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("Complex Email User", savedUser.getName());
        assertEquals("user.name+tag@subdomain.example.co.uk", savedUser.getEmail());
    }

    @Test
    void saveAll_ShouldPersistMultipleUsers() {
        List<User> users = List.of(user1, user2, user3);

        List<User> savedUsers = userRepository.saveAll(users);

        assertEquals(3, savedUsers.size());
        assertTrue(savedUsers.stream().allMatch(user -> user.getId() != null));

        List<User> allUsers = userRepository.findAll();
        assertEquals(3, allUsers.size());
    }

    @Test
    void delete_ShouldRemoveUser() {
        entityManager.persist(user1);
        entityManager.flush();

        userRepository.delete(user1);
        entityManager.flush();

        Optional<User> result = userRepository.findById(user1.getId());
        assertFalse(result.isPresent());
    }

    @Test
    void findById_AfterUpdate_ShouldReturnUpdatedData() {
        entityManager.persist(user1);
        entityManager.flush();

        user1.setName("Changed Name");
        user1.setEmail("changed@example.com");
        userRepository.save(user1);
        entityManager.flush();

        Optional<User> result = userRepository.findById(user1.getId());
        assertTrue(result.isPresent());
        assertEquals("Changed Name", result.get().getName());
        assertEquals("changed@example.com", result.get().getEmail());
    }

    @Test
    void save_ShouldGenerateUniqueIds() {
        User user1 = createUser("test1@example.com", "Test User 1");
        User user2 = createUser("test2@example.com", "Test User 2");
        User user3 = createUser("test3@example.com", "Test User 3");

        User saved1 = userRepository.save(user1);
        User saved2 = userRepository.save(user2);
        User saved3 = userRepository.save(user3);

        assertNotNull(saved1.getId());
        assertNotNull(saved2.getId());
        assertNotNull(saved3.getId());
        assertNotEquals(saved1.getId(), saved2.getId());
        assertNotEquals(saved1.getId(), saved3.getId());
        assertNotEquals(saved2.getId(), saved3.getId());
    }

    private User createUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }
}