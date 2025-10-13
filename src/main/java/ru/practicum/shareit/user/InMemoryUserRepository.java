package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class InMemoryUserRepository {
    private final Map<Integer, User> users = new HashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    public User save(User user) {
        log.debug("Запрос на создание пользователя");
        user.setId(counter.incrementAndGet());
        users.put(user.getId(), user);

        return user;
    }

    public Optional<User> findById(int userId) {
        log.debug("Запрос на получение пользователя с id = {}", userId);
        return Optional.ofNullable(users.get(userId));
    }

    public Optional<User> findByEmail(String email) {
        log.debug("Запрос на получение пользователя с email = {}", email);
        return users.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    public void update(User user) {
        log.debug("Запрос на изменение пользователя с id = {}", user.getId());
        users.put(user.getId(), user);
    }

    public void deleteById(int userId) {
        log.debug("Запрос на удаление пользователя с id = {}", userId);
        users.remove(userId);
    }
}
