package ru.practicum.shareit.server.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ru.practicum.shareit.server.item.model.Item;

import java.util.Collection;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {

    List<Item> findByOwnerId(int ownerId);

    @Query("SELECT item FROM Item as item WHERE " +
            "item.available = TRUE AND " +
            "(UPPER(item.name) LIKE UPPER(CONCAT('%', ?1, '%')) " +
            "OR UPPER(item.description) LIKE UPPER(CONCAT('%', ?1, '%')))")
    List<Item> search(String text);

    List<Item> findByRequestId(int requestId);

    List<Item> findByRequestIdIn(Collection<Integer> requestIds);
}
