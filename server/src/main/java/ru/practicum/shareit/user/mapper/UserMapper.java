package ru.practicum.shareit.user.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Component
public class UserMapper {

    public UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public List<UserDto> toUserDtoList(List<User> users) {
        return users.stream()
                .map(this::toUserDto)
                .toList();
    }

    public User toEntity(UserDto userData) {
        return new User(
                userData.getId(),
                userData.getName(),
                userData.getEmail()
        );
    }
}
