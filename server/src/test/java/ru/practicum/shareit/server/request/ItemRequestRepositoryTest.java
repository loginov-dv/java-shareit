package ru.practicum.shareit.server.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.user.UserRepository;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.utils.ItemRequestTestData;
import ru.practicum.shareit.server.utils.UserTestData;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"/schema.sql", "/clear.sql"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestRepositoryTest {
    private final UserRepository userRepository;
    private final ItemRequestRepository requestRepository;

    @Test
    void shouldSaveRequest() {
        User requestor = userRepository.save(UserTestData.createNewUser());
        ItemRequest request = requestRepository.save(ItemRequestTestData.createNewRequest(requestor));

        assertNotNull(request.getId());
    }

    @Test
    void shouldFindRequestById() {
        User requestor = userRepository.save(UserTestData.createNewUser());
        ItemRequest request = requestRepository.save(ItemRequestTestData.createNewRequest(requestor));
        Optional<ItemRequest> maybeFoundRequest = requestRepository.findById(request.getId());

        if (maybeFoundRequest.isEmpty()) {
            fail();
        }

        ItemRequest foundRequest = maybeFoundRequest.get();

        assertEquals(request.getId(), foundRequest.getId());
        assertEquals(request.getDescription(), foundRequest.getDescription());
        assertEquals(request.getCreated(), foundRequest.getCreated());
        assertEquals(request.getRequestor().getId(), foundRequest.getRequestor().getId());
    }

    @Test
    void shouldFindRequestByRequestorId() {
        User requestor = userRepository.save(UserTestData.createNewUser());
        ItemRequest request = requestRepository.save(ItemRequestTestData.createNewRequest(requestor));
        List<ItemRequest> foundRequests =
                requestRepository.findByRequestorIdOrderByCreatedDesc(request.getRequestor().getId());

        assertEquals(1, foundRequests.size());
        assertTrue(foundRequests.contains(request));
    }

    @Test
    void shouldFindOtherRequests() {
        User requestor = userRepository.save(UserTestData.createNewUser());
        ItemRequest request = requestRepository.save(ItemRequestTestData.createNewRequest(requestor));
        List<ItemRequest> foundRequests =
                requestRepository.findByRequestorIdNotOrderByCreatedDesc(request.getRequestor().getId());

        assertFalse(foundRequests.contains(request));
    }
}