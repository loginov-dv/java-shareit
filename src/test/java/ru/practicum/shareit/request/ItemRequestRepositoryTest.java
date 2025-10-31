package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.request.model.ItemRequest;
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
class ItemRequestRepositoryTest {
    private final UserRepository userRepository;
    private final ItemRequestRepository requestRepository;

    @Test
    void shouldSaveRequest() {
        ItemRequest request = createRequest();
        request = requestRepository.save(request);

        assertNotNull(request.getId());
    }

    @Test
    void shouldFindRequestById() {
        ItemRequest request = createRequest();
        request = requestRepository.save(request);
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
        ItemRequest request = createRequest();
        request = requestRepository.save(request);
        List<ItemRequest> foundRequests = requestRepository.findByRequestorIdOrderByCreatedDesc(request.getRequestor().getId());

        assertEquals(1, foundRequests.size());
        assertTrue(foundRequests.contains(request));
    }

    @Test
    void shouldFindOtherRequests() {
        ItemRequest request = createRequest();
        request = requestRepository.save(request);
        List<ItemRequest> foundRequests = requestRepository.findByRequestorIdNotOrderByCreatedDesc(request.getRequestor().getId());

        assertFalse(foundRequests.contains(request));
    }

    private ItemRequest createRequest() {
        User requestor = createUser();
        ItemRequest request = new ItemRequest();

        request.setRequestor(requestor);
        request.setDescription(RandomUtils.createName(20));

        return request;
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