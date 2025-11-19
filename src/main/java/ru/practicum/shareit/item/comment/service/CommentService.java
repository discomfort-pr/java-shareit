package ru.practicum.shareit.item.comment.service;

import ru.practicum.shareit.item.comment.model.Comment;

public interface CommentService {
    Comment postComment(Long userId, Long itemId, Comment comment);
}
