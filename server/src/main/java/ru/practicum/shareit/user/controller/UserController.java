package ru.practicum.shareit.user.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserController {

    UserService userService;
    UserMapper userMapper;

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userMapper.toUserDtoList(
                userService.getAllUsers()
        );
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long userId) {
        return userMapper.toUserDto(
                userService.getUserById(userId)
        );
    }

    @PostMapping
    public UserDto addUser(@RequestBody UserDto userData) {
        return userMapper.toUserDto(
                userService.addUser(userMapper.toEntity(userData))
        );
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable Long userId, @RequestBody UserDto userData) {
        return userMapper.toUserDto(
                userService.updateUser(userId, userMapper.toEntity(userData))
        );
    }

    @DeleteMapping("/{userId}")
    public UserDto deleteUser(@PathVariable Long userId) {
        return userMapper.toUserDto(
                userService.deleteUser(userId)
        );
    }
}
