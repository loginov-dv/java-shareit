package ru.practicum.shareit.server.request;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.server.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Integer> {
    List<ItemRequest> findByRequestorIdOrderByCreatedDesc(int requestorId);

    List<ItemRequest> findByRequestorIdNotOrderByCreatedDesc(int requestorId);
}
