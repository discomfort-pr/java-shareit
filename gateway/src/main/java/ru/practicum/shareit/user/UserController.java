package ru.practicum.shareit.user;

import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.validation.group.CreateGroup;
import ru.practicum.shareit.validation.group.UpdateGroup;

@RestController
@RequestMapping(path = "/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Validated
public class UserController {

    UserClient userClient;

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        return userClient.get();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserById(@PathVariable @Positive Long userId) {
        return userClient.get(userId);
    }

    @PostMapping
    public ResponseEntity<Object> addUser(@RequestBody @Validated(value = CreateGroup.class) UserDto userData) {
        return userClient.post(userData);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable @Positive Long userId,
                              @RequestBody @Validated(value = UpdateGroup.class) UserDto userData) {
        return userClient.patch(userId, userData);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable @Positive Long userId) {
        return userClient.delete(userId);
    }
}
