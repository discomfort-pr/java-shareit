package ru.practicum.shareit.item.comment.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentMapperTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentMapper commentMapper;

    @Test
    void toEntity_WithValidData_ShouldReturnComment() {
        Long itemId = 1L;
        Long userId = 1L;
        CommentDto commentDto = new CommentDto(null, "Great item!", null, null);

        Item item = new Item();
        item.setId(itemId);
        User user = new User();
        user.setId(userId);
        user.setName("Test User");

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Comment result = commentMapper.toEntity(commentDto, itemId, userId);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("Great item!", result.getText());
        assertEquals(item, result.getItem());
        assertEquals(user, result.getAuthor());
    }

    @Test
    void toEntity_WithNonExistentItem_ShouldThrowItemNotFoundException() {
        Long itemId = 999L;
        Long userId = 1L;
        CommentDto commentDto = new CommentDto(null, "Great item!", null, null);

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        ItemNotFoundException exception = assertThrows(
                ItemNotFoundException.class,
                () -> commentMapper.toEntity(commentDto, itemId, userId)
        );

        assertEquals("Item with id 999 not found", exception.getMessage());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void toEntity_WithNonExistentUser_ShouldThrowUserNotFoundException() {
        Long itemId = 1L;
        Long userId = 999L;
        CommentDto commentDto = new CommentDto(null, "Great item!", null, null);

        Item item = new Item();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> commentMapper.toEntity(commentDto, itemId, userId)
        );

        assertEquals("User with id 999 not found", exception.getMessage());
    }

    @Test
    void toEntity_WithNullCommentDto_ShouldThrowNullPointerException() {
        Long itemId = 1L;
        Long userId = 1L;

        assertThrows(NullPointerException.class, () -> commentMapper.toEntity(null, itemId, userId));
    }

    @Test
    void toCommentDto_WithValidComment_ShouldReturnCommentDto() {
        Long authorId = 1L;
        Long commentId = 1L;

        User author = new User();
        author.setId(authorId);
        author.setName("John Doe");

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setText("Excellent item!");
        comment.setAuthor(author);

        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));

        CommentDto result = commentMapper.toCommentDto(comment, authorId);

        assertNotNull(result);
        assertEquals(commentId, result.getId());
        assertEquals("Excellent item!", result.getText());
        assertEquals("John Doe", result.getAuthorName());
        assertNotNull(result.getCreated());
    }

    @Test
    void toCommentDto_WithNonExistentUser_ShouldThrowUserNotFoundException() {
        Long authorId = 999L;
        Long commentId = 1L;

        User author = new User();
        author.setId(authorId);
        author.setName("John Doe");

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setText("Excellent item!");
        comment.setAuthor(author);

        when(userRepository.findById(authorId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> commentMapper.toCommentDto(comment, authorId)
        );

        assertEquals("User with id 999 not found", exception.getMessage());
    }

    @Test
    void toCommentDto_WithDifferentAuthorId_ShouldUseProvidedAuthorId() {
        Long actualAuthorId = 1L;
        Long providedAuthorId = 2L;
        Long commentId = 1L;

        User actualAuthor = new User();
        actualAuthor.setId(actualAuthorId);
        actualAuthor.setName("Actual Author");

        User providedAuthor = new User();
        providedAuthor.setId(providedAuthorId);
        providedAuthor.setName("Provided Author");

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setText("Test comment");
        comment.setAuthor(actualAuthor);

        when(userRepository.findById(providedAuthorId)).thenReturn(Optional.of(providedAuthor));

        CommentDto result = commentMapper.toCommentDto(comment, providedAuthorId);

        assertEquals("Provided Author", result.getAuthorName());
        verify(userRepository).findById(providedAuthorId);
    }

    @Test
    void toCommentDto_List_WithValidComments_ShouldReturnListOfCommentDtos() {
        Long authorId1 = 1L;
        Long authorId2 = 2L;
        Long commentId1 = 1L;
        Long commentId2 = 2L;

        User author1 = new User();
        author1.setId(authorId1);
        author1.setName("User One");

        User author2 = new User();
        author2.setId(authorId2);
        author2.setName("User Two");

        Comment comment1 = new Comment();
        comment1.setId(commentId1);
        comment1.setText("First comment");
        comment1.setAuthor(author1);

        Comment comment2 = new Comment();
        comment2.setId(commentId2);
        comment2.setText("Second comment");
        comment2.setAuthor(author2);

        List<Comment> comments = List.of(comment1, comment2);

        when(userRepository.findById(authorId1)).thenReturn(Optional.of(author1));
        when(userRepository.findById(authorId2)).thenReturn(Optional.of(author2));

        List<CommentDto> result = commentMapper.toCommentDto(comments);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(commentId1, result.get(0).getId());
        assertEquals("First comment", result.get(0).getText());
        assertEquals("User One", result.get(0).getAuthorName());

        assertEquals(commentId2, result.get(1).getId());
        assertEquals("Second comment", result.get(1).getText());
        assertEquals("User Two", result.get(1).getAuthorName());

        assertNotNull(result.get(0).getCreated());
        assertNotNull(result.get(1).getCreated());
    }

    @Test
    void toCommentDto_List_WithEmptyList_ShouldReturnEmptyList() {
        List<Comment> emptyComments = List.of();

        List<CommentDto> result = commentMapper.toCommentDto(emptyComments);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toCommentDto_List_WithNullList_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> commentMapper.toCommentDto(null));
    }

    @Test
    void toCommentDto_List_WithOneNonExistentUser_ShouldThrowUserNotFoundException() {
        Long existingAuthorId = 1L;
        Long nonExistentAuthorId = 999L;

        User existingAuthor = new User();
        existingAuthor.setId(existingAuthorId);
        existingAuthor.setName("Existing User");

        User nonExistentAuthor = new User();
        nonExistentAuthor.setId(nonExistentAuthorId);

        Comment comment1 = new Comment();
        comment1.setId(1L);
        comment1.setText("First comment");
        comment1.setAuthor(existingAuthor);

        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setText("Second comment");
        comment2.setAuthor(nonExistentAuthor);

        List<Comment> comments = List.of(comment1, comment2);

        when(userRepository.findById(existingAuthorId)).thenReturn(Optional.of(existingAuthor));
        when(userRepository.findById(nonExistentAuthorId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> commentMapper.toCommentDto(comments)
        );

        assertEquals("User with id 999 not found", exception.getMessage());
    }

    @Test
    void toCommentDto_ShouldSetCurrentTimestamp() {
        Long authorId = 1L;
        User author = new User();
        author.setId(authorId);
        author.setName("Test User");

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Test comment");
        comment.setAuthor(author);

        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));

        CommentDto result = commentMapper.toCommentDto(comment, authorId);

        assertNotNull(result.getCreated());
        assertTrue(result.getCreated().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(result.getCreated().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void toEntity_ShouldPreserveTextFromDto() {
        Long itemId = 1L;
        Long userId = 1L;
        String commentText = "This is a very long comment text that should be preserved exactly as is";

        CommentDto commentDto = new CommentDto(null, commentText, null, null);

        Item item = new Item();
        User user = new User();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Comment result = commentMapper.toEntity(commentDto, itemId, userId);

        assertEquals(commentText, result.getText());
    }
}