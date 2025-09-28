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
    public UserDto create(@Valid @RequestBody PostUserRequest request) {
        log.debug("POST /users");
        return userService.add(request);
    }

    @GetMapping("/{userId}")
    public UserDto getById(@PathVariable @Positive int userId) {
        log.debug("GET /users/userId");
        return userService.getById(userId);
    }

    @PatchMapping("/{userId}")
    public UserDto patch(@PathVariable @Positive int userId,
                         @Valid @RequestBody PatchUserRequest request) {
        log.debug("PATCH /users/userId");
        return userService.update(userId, request);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive int userId) {
        log.debug("DELETE /users/userId");
        userService.delete(userId);
    }
}
