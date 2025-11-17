package ru.practicum.shareit.user.controller;

import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.validation.group.CreateGroup;
import ru.practicum.shareit.validation.group.UpdateGroup;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Validated
public class UserController {

    UserService userService;
    UserMapper userMapper;

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userMapper.toUserDto(
                userService.getAllUsers()
        );
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable @Positive Long userId) {
        return userMapper.toUserDto(
                userService.getUserById(userId)
        );
    }

    @PostMapping
    public UserDto addUser(@RequestBody @Validated(value = CreateGroup.class) UserDto userData) {
        return userMapper.toUserDto(
                userService.addUser(userMapper.toEntity(userData))
        );
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable @Positive Long userId,
                              @RequestBody @Validated(value = UpdateGroup.class) UserDto userData) {
        return userMapper.toUserDto(
                userService.updateUser(userId, userMapper.toEntity(userData))
        );
    }

    @DeleteMapping("/{userId}")
    public UserDto deleteUser(@PathVariable @Positive Long userId) {
        return userMapper.toUserDto(
                userService.deleteUser(userId)
        );
    }
}
