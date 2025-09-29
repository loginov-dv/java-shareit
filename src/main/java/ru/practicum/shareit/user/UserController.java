package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.PatchUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.PostUserRequest;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody PostUserRequest request) {
        log.debug("POST /users");
        return userService.createUser(request);
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable @Positive int userId) {
        log.debug("GET /users/userId");
        return userService.findById(userId);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable @Positive int userId,
                              @Valid @RequestBody PatchUserRequest request) {
        log.debug("PATCH /users/userId");
        return userService.update(userId, request);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable @Positive int userId) {
        log.debug("DELETE /users/userId");
        userService.deleteById(userId);
    }
}
