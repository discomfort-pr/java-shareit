package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    void toUserDto_WithValidUser_ShouldReturnUserDto() {
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");

        UserDto result = userMapper.toUserDto(user);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john.doe@example.com", result.getEmail());
    }

    @Test
    void toUserDto_WithUserWithNullId_ShouldReturnUserDtoWithNullId() {
        User user = new User();
        user.setId(null);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");

        UserDto result = userMapper.toUserDto(user);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john.doe@example.com", result.getEmail());
    }

    @Test
    void toUserDto_WithUserWithEmptyName_ShouldReturnUserDtoWithEmptyName() {
        User user = new User();
        user.setId(1L);
        user.setName("");
        user.setEmail("test@example.com");

        UserDto result = userMapper.toUserDto(user);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("", result.getName());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void toUserDto_WithUserWithNullName_ShouldReturnUserDtoWithNullName() {
        User user = new User();
        user.setId(1L);
        user.setName(null);
        user.setEmail("test@example.com");

        UserDto result = userMapper.toUserDto(user);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertNull(result.getName());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void toUserDto_WithUserWithNullEmail_ShouldReturnUserDtoWithNullEmail() {
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail(null);

        UserDto result = userMapper.toUserDto(user);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertNull(result.getEmail());
    }

    @Test
    void toUserDto_WithNullUser_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> userMapper.toUserDto(null));
    }

    @Test
    void toUserDto_List_WithValidUsers_ShouldReturnListOfUserDtos() {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("User One");
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("User Two");
        user2.setEmail("user2@example.com");

        List<User> users = List.of(user1, user2);

        List<UserDto> result = userMapper.toUserDtoList(users);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).getId());
        assertEquals("User One", result.get(0).getName());
        assertEquals("user1@example.com", result.get(0).getEmail());

        assertEquals(2L, result.get(1).getId());
        assertEquals("User Two", result.get(1).getName());
        assertEquals("user2@example.com", result.get(1).getEmail());
    }

    @Test
    void toUserDto_List_WithEmptyList_ShouldReturnEmptyList() {
        List<User> emptyUsers = List.of();

        List<UserDto> result = userMapper.toUserDtoList(emptyUsers);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toUserDto_List_WithNullList_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> userMapper.toUserDtoList((List<User>) null));
    }

    @Test
    void toUserDto_List_WithSingleUser_ShouldReturnSingleElementList() {
        User user = new User();
        user.setId(1L);
        user.setName("Single User");
        user.setEmail("single@example.com");

        List<User> users = List.of(user);

        List<UserDto> result = userMapper.toUserDtoList(users);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Single User", result.get(0).getName());
        assertEquals("single@example.com", result.get(0).getEmail());
    }

    @Test
    void toEntity_WithValidUserDto_ShouldReturnUser() {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Jane Smith");
        userDto.setEmail("jane.smith@example.com");

        User result = userMapper.toEntity(userDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Jane Smith", result.getName());
        assertEquals("jane.smith@example.com", result.getEmail());
    }

    @Test
    void toEntity_WithUserDtoWithNullId_ShouldReturnUserWithNullId() {
        UserDto userDto = new UserDto();
        userDto.setId(null);
        userDto.setName("Jane Smith");
        userDto.setEmail("jane.smith@example.com");

        User result = userMapper.toEntity(userDto);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("Jane Smith", result.getName());
        assertEquals("jane.smith@example.com", result.getEmail());
    }

    @Test
    void toEntity_WithUserDtoWithEmptyName_ShouldReturnUserWithEmptyName() {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("");
        userDto.setEmail("test@example.com");

        User result = userMapper.toEntity(userDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("", result.getName());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void toEntity_WithUserDtoWithNullName_ShouldReturnUserWithNullName() {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName(null);
        userDto.setEmail("test@example.com");

        User result = userMapper.toEntity(userDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertNull(result.getName());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void toEntity_WithUserDtoWithNullEmail_ShouldReturnUserWithNullEmail() {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("John Doe");
        userDto.setEmail(null);

        User result = userMapper.toEntity(userDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertNull(result.getEmail());
    }

    @Test
    void toEntity_WithNullUserDto_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> userMapper.toEntity(null));
    }

    @Test
    void toEntity_WithUserDtoUsingConstructor_ShouldReturnCorrectUser() {
        UserDto userDto = new UserDto(1L, "Constructor User", "constructor@example.com");

        User result = userMapper.toEntity(userDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Constructor User", result.getName());
        assertEquals("constructor@example.com", result.getEmail());
    }

    @Test
    void toUserDto_WithUserUsingConstructor_ShouldReturnCorrectUserDto() {
        User user = new User(1L, "Constructor User", "constructor@example.com");

        UserDto result = userMapper.toUserDto(user);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Constructor User", result.getName());
        assertEquals("constructor@example.com", result.getEmail());
    }

    @Test
    void toUserDto_ShouldPreserveAllFieldsExactly() {
        User user = new User();
        user.setId(123L);
        user.setName("Test User With Long Name");
        user.setEmail("very.long.email.address@subdomain.example.com");

        UserDto result = userMapper.toUserDto(user);

        assertEquals(123L, result.getId());
        assertEquals("Test User With Long Name", result.getName());
        assertEquals("very.long.email.address@subdomain.example.com", result.getEmail());
    }

    @Test
    void toEntity_ShouldPreserveAllFieldsExactly() {
        UserDto userDto = new UserDto();
        userDto.setId(456L);
        userDto.setName("Another Test User");
        userDto.setEmail("another.test@example.org");

        User result = userMapper.toEntity(userDto);

        assertEquals(456L, result.getId());
        assertEquals("Another Test User", result.getName());
        assertEquals("another.test@example.org", result.getEmail());
    }

    @Test
    void toUserDto_List_WithMixedUsers_ShouldHandleAllCases() {
        User user1 = new User(1L, "User One", "user1@example.com");
        User user2 = new User(2L, null, "user2@example.com");
        User user3 = new User(3L, "User Three", null);
        User user4 = new User(null, "User Four", "user4@example.com");

        List<User> users = List.of(user1, user2, user3, user4);

        List<UserDto> result = userMapper.toUserDtoList(users);

        assertEquals(4, result.size());

        assertEquals(1L, result.get(0).getId());
        assertEquals("User One", result.get(0).getName());
        assertEquals("user1@example.com", result.get(0).getEmail());

        assertEquals(2L, result.get(1).getId());
        assertNull(result.get(1).getName());
        assertEquals("user2@example.com", result.get(1).getEmail());

        assertEquals(3L, result.get(2).getId());
        assertEquals("User Three", result.get(2).getName());
        assertNull(result.get(2).getEmail());

        assertNull(result.get(3).getId());
        assertEquals("User Four", result.get(3).getName());
        assertEquals("user4@example.com", result.get(3).getEmail());
    }

    @Test
    void toEntity_WithUserDtoHavingSpecialCharacters_ShouldPreserveThem() {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("User with spéciäl chàràctêrs");
        userDto.setEmail("user+tag@example.com");

        User result = userMapper.toEntity(userDto);

        assertEquals(1L, result.getId());
        assertEquals("User with spéciäl chàràctêrs", result.getName());
        assertEquals("user+tag@example.com", result.getEmail());
    }
}