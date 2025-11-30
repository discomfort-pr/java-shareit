package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UserDto userDto;
    private User user;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("John Doe");
        userDto.setEmail("john.doe@example.com");

        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        List<User> users = List.of(user);
        List<UserDto> userDtos = List.of(userDto);

        when(userService.getAllUsers()).thenReturn(users);
        when(userMapper.toUserDtoList(users)).thenReturn(userDtos);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));
    }

    @Test
    void getAllUsers_WithNoUsers_ShouldReturnEmptyList() throws Exception {
        List<User> emptyUsers = List.of();
        List<UserDto> emptyDtos = List.of();

        when(userService.getAllUsers()).thenReturn(emptyUsers);
        when(userMapper.toUserDtoList(emptyUsers)).thenReturn(emptyDtos);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getUserById_WithExistingUser_ShouldReturnUser() throws Exception {
        Long userId = 1L;

        when(userService.getUserById(userId)).thenReturn(user);
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void addUser_WithValidData_ShouldReturnCreatedUser() throws Exception {
        UserDto requestDto = new UserDto();
        requestDto.setName("Jane Smith");
        requestDto.setEmail("jane.smith@example.com");

        UserDto responseDto = new UserDto();
        responseDto.setId(2L);
        responseDto.setName("Jane Smith");
        responseDto.setEmail("jane.smith@example.com");

        User userToSave = new User();
        userToSave.setName("Jane Smith");
        userToSave.setEmail("jane.smith@example.com");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setName("Jane Smith");
        savedUser.setEmail("jane.smith@example.com");

        when(userMapper.toEntity(requestDto)).thenReturn(userToSave);
        when(userService.addUser(userToSave)).thenReturn(savedUser);
        when(userMapper.toUserDto(savedUser)).thenReturn(responseDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("Jane Smith"))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"));
    }

    @Test
    void updateUser_WithValidData_ShouldReturnUpdatedUser() throws Exception {
        Long userId = 1L;
        UserDto updateDto = new UserDto();
        updateDto.setName("Updated Name");
        updateDto.setEmail("updated@example.com");

        UserDto responseDto = new UserDto();
        responseDto.setId(1L);
        responseDto.setName("Updated Name");
        responseDto.setEmail("updated@example.com");

        User userToUpdate = new User();
        userToUpdate.setName("Updated Name");
        userToUpdate.setEmail("updated@example.com");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("Updated Name");
        updatedUser.setEmail("updated@example.com");

        when(userMapper.toEntity(updateDto)).thenReturn(userToUpdate);
        when(userService.updateUser(userId, userToUpdate)).thenReturn(updatedUser);
        when(userMapper.toUserDto(updatedUser)).thenReturn(responseDto);

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void updateUser_WithPartialData_ShouldReturnUpdatedUser() throws Exception {
        Long userId = 1L;
        UserDto partialUpdateDto = new UserDto();
        partialUpdateDto.setName("Only Name Updated");

        UserDto responseDto = new UserDto();
        responseDto.setId(1L);
        responseDto.setName("Only Name Updated");
        responseDto.setEmail("john.doe@example.com"); // Email остается прежним

        User userToUpdate = new User();
        userToUpdate.setName("Only Name Updated");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("Only Name Updated");
        updatedUser.setEmail("john.doe@example.com");

        when(userMapper.toEntity(partialUpdateDto)).thenReturn(userToUpdate);
        when(userService.updateUser(userId, userToUpdate)).thenReturn(updatedUser);
        when(userMapper.toUserDto(updatedUser)).thenReturn(responseDto);

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Only Name Updated"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void deleteUser_ShouldReturnDeletedUser() throws Exception {
        Long userId = 1L;

        when(userService.deleteUser(userId)).thenReturn(user);
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void getAllUsers_WithMultipleUsers_ShouldReturnAllUsers() throws Exception {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("User One");
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("User Two");
        user2.setEmail("user2@example.com");

        UserDto userDto1 = new UserDto();
        userDto1.setId(1L);
        userDto1.setName("User One");
        userDto1.setEmail("user1@example.com");

        UserDto userDto2 = new UserDto();
        userDto2.setId(2L);
        userDto2.setName("User Two");
        userDto2.setEmail("user2@example.com");

        List<User> users = List.of(user1, user2);
        List<UserDto> userDtos = List.of(userDto1, userDto2);

        when(userService.getAllUsers()).thenReturn(users);
        when(userMapper.toUserDtoList(users)).thenReturn(userDtos);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("User One"))
                .andExpect(jsonPath("$[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("User Two"))
                .andExpect(jsonPath("$[1].email").value("user2@example.com"));
    }

    @Test
    void updateUser_WithEmptyBody_ShouldHandleGracefully() throws Exception {
        Long userId = 1L;

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }
}