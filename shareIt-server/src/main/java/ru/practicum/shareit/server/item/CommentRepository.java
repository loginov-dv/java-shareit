package ru.practicum.shareit.server.item;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.practicum.shareit.server.item.model.Comment;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findByItemId(int itemId);

    List<Comment> findByItemIdIn(Collection<Integer> itemIds);
}
