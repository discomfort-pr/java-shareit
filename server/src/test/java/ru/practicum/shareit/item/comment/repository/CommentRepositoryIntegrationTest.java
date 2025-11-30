package ru.practicum.shareit.item.comment.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(scripts = {"/test-schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CommentRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

    private User user1;
    private User user2;
    private Item item1;
    private Item item2;
    private Item item3;

    @BeforeEach
    void setUp() {
        user1 = createUser("user1@example.com", "User One");
        user2 = createUser("user2@example.com", "User Two");

        entityManager.persist(user1);
        entityManager.persist(user2);

        item1 = createItem("Item 1", "Description 1", user1, true);
        item2 = createItem("Item 2", "Description 2", user1, true);
        item3 = createItem("Item 3", "Description 3", user2, true);

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(item3);

        entityManager.flush();
    }

    @Test
    void findByItemId_WithExistingComments_ShouldReturnCommentsForItem() {
        Comment comment1 = createComment("Great item!", item1, user2);
        Comment comment2 = createComment("Works perfectly", item1, user2);
        Comment comment3 = createComment("Nice quality", item2, user1);

        entityManager.persist(comment1);
        entityManager.persist(comment2);
        entityManager.persist(comment3);
        entityManager.flush();

        List<Comment> result = commentRepository.findByItemId(item1.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(comment -> comment.getItem().getId().equals(item1.getId())));
        assertTrue(result.stream().anyMatch(comment -> comment.getText().equals("Great item!")));
        assertTrue(result.stream().anyMatch(comment -> comment.getText().equals("Works perfectly")));
    }

    @Test
    void findByItemId_WithNoComments_ShouldReturnEmptyList() {
        List<Comment> result = commentRepository.findByItemId(item1.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findByItemId_WithCommentsOnDifferentItems_ShouldReturnOnlySpecifiedItemComments() {
        Comment comment1 = createComment("Comment on item1", item1, user2);
        Comment comment2 = createComment("Another comment on item1", item1, user1);
        Comment comment3 = createComment("Comment on item2", item2, user2);
        Comment comment4 = createComment("Comment on item3", item3, user1);

        entityManager.persist(comment1);
        entityManager.persist(comment2);
        entityManager.persist(comment3);
        entityManager.persist(comment4);
        entityManager.flush();

        List<Comment> result = commentRepository.findByItemId(item1.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(comment -> comment.getItem().getId().equals(item1.getId())));
        assertTrue(result.stream().anyMatch(comment -> comment.getText().equals("Comment on item1")));
        assertTrue(result.stream().anyMatch(comment -> comment.getText().equals("Another comment on item1")));
    }

    @Test
    void findByItemId_WithNonExistingItemId_ShouldReturnEmptyList() {
        Comment comment = createComment("Test comment", item1, user2);
        entityManager.persist(comment);
        entityManager.flush();

        List<Comment> result = commentRepository.findByItemId(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void save_ShouldPersistCommentWithCorrectRelations() {
        Comment newComment = createComment("New comment text", item1, user2);

        Comment savedComment = commentRepository.save(newComment);

        assertNotNull(savedComment.getId());
        assertEquals("New comment text", savedComment.getText());
        assertEquals(item1.getId(), savedComment.getItem().getId());
        assertEquals(user2.getId(), savedComment.getAuthor().getId());

        Comment retrieved = commentRepository.findById(savedComment.getId()).orElse(null);
        assertNotNull(retrieved);
        assertEquals(savedComment.getId(), retrieved.getId());
        assertEquals("New comment text", retrieved.getText());
        assertEquals(item1.getId(), retrieved.getItem().getId());
        assertEquals(user2.getId(), retrieved.getAuthor().getId());
    }

    @Test
    void findById_WithExistingId_ShouldReturnComment() {
        Comment comment = createComment("Test comment", item1, user2);
        entityManager.persist(comment);
        entityManager.flush();

        Comment result = commentRepository.findById(comment.getId()).orElse(null);

        assertNotNull(result);
        assertEquals(comment.getId(), result.getId());
        assertEquals("Test comment", result.getText());
        assertEquals(item1.getId(), result.getItem().getId());
        assertEquals(user2.getId(), result.getAuthor().getId());
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        assertTrue(commentRepository.findById(999L).isEmpty());
    }

    @Test
    void findAll_ShouldReturnAllComments() {
        Comment comment1 = createComment("Comment 1", item1, user2);
        Comment comment2 = createComment("Comment 2", item2, user1);
        Comment comment3 = createComment("Comment 3", item3, user2);

        entityManager.persist(comment1);
        entityManager.persist(comment2);
        entityManager.persist(comment3);
        entityManager.flush();

        List<Comment> result = commentRepository.findAll();

        assertEquals(3, result.size());
    }

    @Test
    void deleteById_ShouldRemoveComment() {
        Comment comment = createComment("To delete", item1, user2);
        entityManager.persist(comment);
        entityManager.flush();

        Long commentId = comment.getId();
        commentRepository.deleteById(commentId);
        entityManager.flush();

        assertTrue(commentRepository.findById(commentId).isEmpty());
    }

    @Test
    void findByItemId_ShouldMaintainCommentOrder() {
        Comment comment1 = createComment("First comment", item1, user2);
        Comment comment2 = createComment("Second comment", item1, user1);
        Comment comment3 = createComment("Third comment", item1, user2);

        entityManager.persist(comment1);
        entityManager.persist(comment2);
        entityManager.persist(comment3);
        entityManager.flush();

        List<Comment> result = commentRepository.findByItemId(item1.getId());

        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getText().equals("First comment")));
        assertTrue(result.stream().anyMatch(c -> c.getText().equals("Second comment")));
        assertTrue(result.stream().anyMatch(c -> c.getText().equals("Third comment")));
    }

    @Test
    void findByItemId_WithMultipleItemsAndUsers_ShouldReturnCorrectComments() {
        Comment comment1 = createComment("User2 on Item1", item1, user2);
        Comment comment2 = createComment("User1 on Item1", item1, user1);
        Comment comment3 = createComment("User2 on Item2", item2, user2);
        Comment comment4 = createComment("User1 on Item3", item3, user1);

        entityManager.persist(comment1);
        entityManager.persist(comment2);
        entityManager.persist(comment3);
        entityManager.persist(comment4);
        entityManager.flush();

        List<Comment> resultItem1 = commentRepository.findByItemId(item1.getId());
        List<Comment> resultItem2 = commentRepository.findByItemId(item2.getId());
        List<Comment> resultItem3 = commentRepository.findByItemId(item3.getId());

        assertEquals(2, resultItem1.size());
        assertEquals(1, resultItem2.size());
        assertEquals(1, resultItem3.size());

        assertTrue(resultItem1.stream().allMatch(c -> c.getItem().getId().equals(item1.getId())));
        assertTrue(resultItem2.stream().allMatch(c -> c.getItem().getId().equals(item2.getId())));
        assertTrue(resultItem3.stream().allMatch(c -> c.getItem().getId().equals(item3.getId())));
    }

    @Test
    void updateComment_ShouldModifyExistingComment() {
        Comment comment = createComment("Original text", item1, user2);
        entityManager.persist(comment);
        entityManager.flush();

        comment.setText("Updated text");
        Comment updatedComment = commentRepository.save(comment);

        assertEquals(comment.getId(), updatedComment.getId());
        assertEquals("Updated text", updatedComment.getText());

        Comment fromDb = commentRepository.findById(comment.getId()).orElse(null);
        assertNotNull(fromDb);
        assertEquals("Updated text", fromDb.getText());
    }

    private User createUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }

    private Item createItem(String name, String description, User owner, Boolean available) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(owner);
        return item;
    }

    private Comment createComment(String text, Item item, User author) {
        Comment comment = new Comment();
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(author);
        return comment;
    }
}