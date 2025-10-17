package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
// используем настройки из application-test.properties
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"/schema.sql"})
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveUser() {
        User user = createUser();

        user = userRepository.save(user);

        assertNotNull(user.getId());
    }

    @Test
    void shouldFindUser() {
        User user = createUser();

        user = userRepository.save(user);

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
        User user = createUser();

        user = userRepository.save(user);

        userRepository.deleteById(user.getId());

        Optional<User> maybeFoundUser = userRepository.findById(user.getId());

        if (maybeFoundUser.isPresent()) {
            fail();
        }
    }

    @Test
    void shouldFindByEmail() {
        User user = createUser();

        user = userRepository.save(user);

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
        User user = createUser();

        user = userRepository.save(user);

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

    private User createUser() {
        User user = new User();
        String name = createName();

        user.setName(name);
        user.setEmail(name + "@mail.ru");

        return user;
    }

    private String createName() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        int charsLength = chars.length();
        int counter = 0;
        int length = 10;
        String result = "";

        while (counter < length) {
            result += chars.charAt((int)Math.round(Math.random() * (charsLength - 1)));
            counter++;
        }

        return result;
    }
}

