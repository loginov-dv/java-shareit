package ru.practicum.shareit.gateway.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.practicum.shareit.gateway.user.dto.PatchUserRequest;
import ru.practicum.shareit.gateway.user.dto.PostUserRequest;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserClient userClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createUser(@Valid @RequestBody PostUserRequest request) {
        log.debug("gateway: POST /users");
        return userClient.createUser(request);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUser(@PathVariable @Positive int userId) {
        log.debug("gateway: GET /users/{}", userId);
        return userClient.getUser(userId);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable @Positive int userId,
                              @Valid @RequestBody PatchUserRequest request) {
        log.debug("gateway: PATCH /users/{}", userId);
        return userClient.updateUser(userId, request);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Object> deleteUser(@PathVariable @Positive int userId) {
        log.debug("gateway: DELETE /users/{}", userId);
        return userClient.deleteUser(userId);
    }
}