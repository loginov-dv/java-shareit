package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Repository
public class InMemoryUserRepository implements UserRepository {
    private final Map<Integer, User> users = new HashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public User save(User user) {
        log.debug("Запрос на создание пользователя");
        user.setId(counter.incrementAndGet());
        users.put(user.getId(), user);

        return user;
    }

    @Override
    public Optional<User> findById(int userId) {
        log.debug("Запрос на получение пользователя с id = {}", userId);
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        log.debug("Запрос на получение пользователя с email = {}", email);
        return users.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public void update(User user) {
        log.debug("Запрос на изменение пользователя с id = {}", user.getId());
        users.put(user.getId(), user);
    }

    @Override
    public void deleteById(int userId) {
        log.debug("Запрос на удаление пользователя с id = {}", userId);
        users.remove(userId);
    }
}
