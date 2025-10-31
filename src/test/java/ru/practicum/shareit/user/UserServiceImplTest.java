package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.exception.EmailConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.PatchUserRequest;
import ru.practicum.shareit.user.dto.PostUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utils.RandomUtils;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
class UserServiceImplTest {
    @Autowired
    private UserService userService;
    @MockBean
    private UserRepository userRepository;

    private final Random random = new Random();

    @Test
    void shouldCreateUser() {
        PostUserRequest request = createPostUserRequest();

        User savedUser = new User();
        savedUser.setId(random.nextInt(100));
        savedUser.setName(request.getName());
        savedUser.setEmail(request.getEmail());

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserDto result = userService.createUser(request);

        assertEquals(savedUser.getId(), result.getId());
        assertEquals(savedUser.getName(), result.getName());
        assertEquals(savedUser.getEmail(), result.getEmail());
    }

    @Test
    void shouldNotCreateUserWithAlreadyExistingEmail() {
        PostUserRequest request = createPostUserRequest();

        User existingUser = new User();
        existingUser.setId(random.nextInt(100));
        existingUser.setName(RandomUtils.createName());
        existingUser.setEmail(request.getEmail());

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(existingUser));

        assertThrows(EmailConflictException.class, () -> userService.createUser(request));
    }

    @Test
    void shouldFindUserById() {
        User savedUser = new User();
        savedUser.setId(random.nextInt(100));
        savedUser.setName(RandomUtils.createName());
        savedUser.setEmail(savedUser.getName() + "@mail.ru");

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(savedUser));

        UserDto result = userService.findById(savedUser.getId());

        assertEquals(savedUser.getId(), result.getId());
        assertEquals(savedUser.getName(), result.getName());
        assertEquals(savedUser.getEmail(), result.getEmail());
    }

    @Test
    void shouldNotFindUnknownUserById() {
        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.findById(random.nextInt(100)));
    }

    @Test
    void shouldDeleteUserById() {
        doNothing().when(userRepository).deleteById(anyInt());

        int randomInt = random.nextInt(100);

        userService.deleteById(randomInt);

        verify(userRepository, Mockito.times(1)).deleteById(randomInt);
    }

    @Test
    void shouldUpdateUser() {
        PatchUserRequest request = createPatchUserRequest();

        User existingUser = new User();
        existingUser.setId(random.nextInt(100));
        existingUser.setName(RandomUtils.createName());
        existingUser.setEmail(existingUser.getName() + "@mail.ru");

        User updatedUser = new User();
        updatedUser.setId(existingUser.getId());
        updatedUser.setName(request.getName());
        updatedUser.setEmail(request.getEmail());

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenReturn(updatedUser);

        UserDto result = userService.update(existingUser.getId(), request);

        assertEquals(updatedUser.getId(), result.getId());
        assertEquals(updatedUser.getName(), result.getName());
        assertEquals(updatedUser.getEmail(), result.getEmail());
    }

    @Test
    void shouldNotUpdateUnknownUser() {
        PatchUserRequest request = createPatchUserRequest();

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.update(random.nextInt(100), request));
    }

    @Test
    void shouldNotUpdateUserIfNewEmailAlreadyExists() {
        PatchUserRequest request = createPatchUserRequest();

        User existingUser = new User();
        existingUser.setId(random.nextInt(100));
        existingUser.setName(RandomUtils.createName());
        existingUser.setEmail(existingUser.getName() + "@mail.ru");

        User userWithTheSameEmail = new User();
        userWithTheSameEmail.setId(random.nextInt(100));
        userWithTheSameEmail.setName(RandomUtils.createName());
        userWithTheSameEmail.setEmail(request.getEmail());

        when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(userWithTheSameEmail));

        assertThrows(EmailConflictException.class, () -> userService.update(existingUser.getId(), request));
    }

    private PatchUserRequest createPatchUserRequest() {
        PatchUserRequest request = new PatchUserRequest();

        request.setName(RandomUtils.createName());
        request.setEmail(request.getName() + "@mail.ru");

        return request;
    }

    private PostUserRequest createPostUserRequest() {
        PostUserRequest request = new PostUserRequest();

        request.setName(RandomUtils.createName());
        request.setEmail(request.getName() + "@mail.ru");

        return request;
    }
}