package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utils.ItemTestData;
import ru.practicum.shareit.utils.UserTestData;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"/schema.sql", "/clear.sql"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CommentRepositoryTest {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Test
    void shouldSaveComment() {
        User owner = userRepository.save(UserTestData.createNewUser());
        Item item = itemRepository.save(ItemTestData.createNewItem(owner));
        User author = userRepository.save(UserTestData.createNewUser());
        Comment comment = commentRepository.save(ItemTestData.createNewComment(item, author));

        assertNotNull(comment.getId());
    }

    @Test
    void shouldFindCommentById() {
        User owner = userRepository.save(UserTestData.createNewUser());
        Item item = itemRepository.save(ItemTestData.createNewItem(owner));
        User author = userRepository.save(UserTestData.createNewUser());
        Comment comment = commentRepository.save(ItemTestData.createNewComment(item, author));

        Optional<Comment> maybeFoundComment = commentRepository.findById(comment.getId());

        if (maybeFoundComment.isEmpty()) {
            fail();
        }

        Comment foundComment = maybeFoundComment.get();

        assertEquals(comment.getId(), foundComment.getId());
        assertEquals(comment.getText(), foundComment.getText());
        assertEquals(comment.getCreated(), foundComment.getCreated());
        assertEquals(comment.getItem().getId(), foundComment.getItem().getId());
        assertEquals(comment.getAuthor().getId(), foundComment.getAuthor().getId());
    }

    @Test
    void shouldFindCommentsByItemId() {
        User owner = userRepository.save(UserTestData.createNewUser());
        Item item = itemRepository.save(ItemTestData.createNewItem(owner));

        User author1 = userRepository.save(UserTestData.createNewUser());
        User author2 = userRepository.save(UserTestData.createNewUser());

        Comment comment1 = commentRepository.save(ItemTestData.createNewComment(item, author1));
        Comment comment2 = commentRepository.save(ItemTestData.createNewComment(item, author2));

        List<Comment> commentList = commentRepository.findByItemId(item.getId());

        assertEquals(2, commentList.size());
        assertTrue(commentList.contains(comment1));
        assertTrue(commentList.contains(comment2));
    }

    @Test
    void shouldFindCommentsByItemIdIn() {
        User owner = userRepository.save(UserTestData.createNewUser());

        Item item1 = itemRepository.save(ItemTestData.createNewItem(owner));
        Item item2 = itemRepository.save(ItemTestData.createNewItem(owner));

        User author = userRepository.save(UserTestData.createNewUser());

        Comment comment1 = commentRepository.save(ItemTestData.createNewComment(item1, author));
        Comment comment2 = commentRepository.save(ItemTestData.createNewComment(item2, author));

        List<Comment> commentList = commentRepository.findByItemIdIn(List.of(item1.getId(), item2.getId()));

        assertEquals(2, commentList.size());
        assertTrue(commentList.contains(comment1));
        assertTrue(commentList.contains(comment2));
    }
}