package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public User save(User user) {
        user.setId(getId());
        users.put(user.getId(), user);

        return user;
    }

    @Override
    public Optional<User> findById(int userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public void update(User user) {
        users.put(user.getId(), user);
    }

    @Override
    public void delete(int userId) {
        users.remove(userId);
    }

    private int getId() {
        int lastId = users.keySet().stream()
                .max(Integer::compareTo)
                .orElse(0);

        return lastId + 1;
    }
}
