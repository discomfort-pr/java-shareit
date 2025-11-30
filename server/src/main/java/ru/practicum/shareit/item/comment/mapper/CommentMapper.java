package ru.practicum.shareit.item.comment.mapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentMapper {

    ItemRepository itemRepository;
    UserRepository userRepository;

    public Comment toEntity(CommentDto commentData, Long itemId, Long userId) {
        return new Comment(
                null,
                commentData.getText(),
                itemRepository.findById(itemId)
                        .orElseThrow(() -> new ItemNotFoundException(
                                String.format("Item with id %d not found", itemId)
                        )),
                userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException(
                                String.format("User with id %d not found", userId)
                        ))
        );
    }

    public CommentDto toCommentDto(Comment comment, Long authorId) {
        String userName = userRepository.findById(authorId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User with id %d not found", authorId)
                )).getName();

        return new CommentDto(
                comment.getId(),
                comment.getText(),
                userName,
                LocalDateTime.now()
        );
    }

    public List<CommentDto> toCommentDto(List<Comment> comments) {
        return comments.stream()
                .map(comment -> toCommentDto(comment, comment.getAuthor().getId()))
                .toList();
    }
}
