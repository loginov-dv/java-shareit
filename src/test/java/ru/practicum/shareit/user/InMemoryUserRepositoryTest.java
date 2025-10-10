package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserRepositoryTest {
    private final InMemoryUserRepository userRepository;

    private InMemoryUserRepositoryTest() {
        userRepository = new InMemoryUserRepository();
    }

    @Test
    void shouldSaveUser() {
        User user = createUser();

        user = userRepository.save(user);

        assertNotNull(user.getId());
        assertNotEquals(0, user.getId());
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

        userRepository.update(user);

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

