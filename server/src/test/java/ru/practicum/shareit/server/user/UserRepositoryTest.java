package ru.practicum.shareit.server.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.utils.UserTestData;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"/schema.sql", "/clear.sql"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserRepositoryTest {
    private final UserRepository userRepository;

    @Test
    void shouldSaveUser() {
        User user = userRepository.save(UserTestData.createNewUser());

        assertNotNull(user.getId());
    }

    @Test
    void shouldFindUserById() {
        User user = userRepository.save(UserTestData.createNewUser());
        Optional<User> maybeFoundUser = userRepository.findById(user.getId());

        if (maybeFoundUser.isEmpty()) {
            fail();
        }

        User foundUser = maybeFoundUser.get();

        assertEquals(user.getId(), foundUser.getId());
        assertEquals(user.getName(), foundUser.getName());
        assertEquals(user.getEmail(), foundUser.getEmail());
    }

    @Test
    void shouldNotFindUnknownUser() {
        Optional<User> maybeFoundUser = userRepository.findById(9999);

        if (maybeFoundUser.isPresent()) {
            fail();
        }
    }

    @Test
    void shouldDeleteUser() {
        User user = userRepository.save(UserTestData.createNewUser());

        userRepository.deleteById(user.getId());

        Optional<User> maybeFoundUser = userRepository.findById(user.getId());

        if (maybeFoundUser.isPresent()) {
            fail();
        }
    }

    @Test
    void shouldFindUserByEmail() {
        User user = userRepository.save(UserTestData.createNewUser());
        Optional<User> maybeFoundUser = userRepository.findByEmail(user.getEmail());

        if (maybeFoundUser.isEmpty()) {
            fail();
        }

        User foundUser = maybeFoundUser.get();

        assertEquals(user.getId(), foundUser.getId());
        assertEquals(user.getName(), foundUser.getName());
        assertEquals(user.getEmail(), foundUser.getEmail());
    }

    @Test
    void shouldUpdateUser() {
        User user = userRepository.save(UserTestData.createNewUser());

        user.setName("new name");
        user.setEmail("new@mail.ru");
        userRepository.save(user);

        Optional<User> maybeFoundUser = userRepository.findById(user.getId());

        if (maybeFoundUser.isEmpty()) {
            fail();
        }

        User foundUser = maybeFoundUser.get();

        assertEquals(user.getId(), foundUser.getId());
        assertEquals(user.getName(), foundUser.getName());
        assertEquals(user.getEmail(), foundUser.getEmail());
    }
}

