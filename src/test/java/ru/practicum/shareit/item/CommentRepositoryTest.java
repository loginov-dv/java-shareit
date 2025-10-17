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
import ru.practicum.shareit.utils.RandomUtils;

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
        Comment comment = createComment();
        comment = commentRepository.save(comment);

        assertNotNull(comment.getId());
    }

    @Test
    void shouldFindCommentById() {
        Comment comment = createComment();
        comment = commentRepository.save(comment);
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
        Item item = createItem();
        Comment comment1 = createComment(item);
        Comment comment2 = createComment(item);
        comment1 = commentRepository.save(comment1);
        comment2 = commentRepository.save(comment2);

        List<Comment> commentList = commentRepository.findByItemId(item.getId());

        assertEquals(2, commentList.size());
        assertTrue(commentList.contains(comment1));
        assertTrue(commentList.contains(comment2));
    }

    @Test
    void shouldFindCommentsByItemIdIn() {
        Item item1 = createItem();
        Item item2 = createItem();
        Comment comment1 = createComment(item1);
        Comment comment2 = createComment(item2);
        comment1 = commentRepository.save(comment1);
        comment2 = commentRepository.save(comment2);

        List<Comment> commentList = commentRepository.findByItemIdIn(List.of(item1.getId(), item2.getId()));

        assertEquals(2, commentList.size());
        assertTrue(commentList.contains(comment1));
        assertTrue(commentList.contains(comment2));
    }

    private Comment createComment() {
        Item item = createItem();

        return createComment(item);
    }

    private Comment createComment(Item item) {
        Comment comment = new Comment();

        comment.setItem(item);
        comment.setAuthor(item.getOwner());
        comment.setText(RandomUtils.createName(100));

        return comment;
    }

    private Item createItem() {
        User user = createUser();
        Item item = new Item();

        item.setName(RandomUtils.createName());
        item.setDescription(RandomUtils.createName(50));
        item.setAvailable(true);
        item.setOwner(user);

        item = itemRepository.save(item);

        return item;
    }

    private User createUser() {
        User user = new User();
        String name = RandomUtils.createName();

        user.setName(name);
        user.setEmail(name + "@mail.ru");

        user = userRepository.save(user);

        return user;
    }

}