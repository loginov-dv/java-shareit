package ru.practicum.shareit.server.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.exception.EmailConflictException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.user.dto.NewUserDto;
import ru.practicum.shareit.server.user.dto.UpdateUserDto;
import ru.practicum.shareit.server.user.dto.UserDto;
import ru.practicum.shareit.server.utils.UserTestData;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@Transactional
@DataJpaTest
@Import(UserServiceImpl.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"/schema.sql", "/clear.sql"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplIntegrationTest {
    private final UserService userService;

    @Test
    void shouldCreateUser() {
        NewUserDto request = UserTestData.createNewUserDto();
        UserDto savedUser = userService.createUser(request);

        assertNotNull(savedUser.getId());
        assertEquals(request.getName(), savedUser.getName());
        assertEquals(request.getEmail(), savedUser.getEmail());
    }

    @Test
    void shouldNotCreateUserWithAlreadyExistingEmail() {
        UserDto alreadyExistingUser = userService.createUser(UserTestData.createNewUserDto());
        NewUserDto request = UserTestData.createNewUserDto();
        request.setEmail(alreadyExistingUser.getEmail());

        assertThrows(EmailConflictException.class, () -> userService.createUser(request));
    }

    @Test
    void shouldFindUserById() {
        UserDto savedUser = userService.createUser(UserTestData.createNewUserDto());
        UserDto foundUser = userService.findById(savedUser.getId());

        assertEquals(savedUser.getId(), foundUser.getId());
        assertEquals(savedUser.getName(), foundUser.getName());
        assertEquals(savedUser.getEmail(), foundUser.getEmail());
    }

    @Test
    void shouldNotFindUnknownUserById() {
        assertThrows(NotFoundException.class, () -> userService.findById(999));
    }

    @Test
    void shouldDeleteUserById() {
        UserDto savedUser = userService.createUser(UserTestData.createNewUserDto());

        assertNotNull(savedUser.getId());

        userService.deleteById(savedUser.getId());

        assertThrows(NotFoundException.class, () -> userService.findById(savedUser.getId()));
    }

    @Test
    void shouldUpdateUser() {
        UserDto savedUser = userService.createUser(UserTestData.createNewUserDto());
        UpdateUserDto request = UserTestData.createUpdateUserDto();

        UserDto updatedUser = userService.update(savedUser.getId(), request);

        assertEquals(savedUser.getId(), updatedUser.getId());
        assertEquals(request.getName(), updatedUser.getName());
        assertEquals(request.getEmail(), updatedUser.getEmail());
    }

    @Test
    void shouldNotUpdateUnknownUser() {
        assertThrows(NotFoundException.class,
                () -> userService.update(999, UserTestData.createUpdateUserDto()));
    }

    @Test
    void shouldNotUpdateUserIfNewEmailAlreadyExists() {
        UserDto userToBeUpdated = userService.createUser(UserTestData.createNewUserDto());
        UserDto userWithCoolEmail = userService.createUser(UserTestData.createNewUserDto());
        UpdateUserDto request = UserTestData.createUpdateUserDto();
        request.setEmail(userWithCoolEmail.getEmail());

        assertThrows(EmailConflictException.class,
                () -> userService.update(userToBeUpdated.getId(), request));
    }
}