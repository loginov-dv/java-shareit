package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(int userId);

    Optional<User> findByEmail(String email);

    void update(User user);

    void delete(int userId);
}
