package ru.practicum.shareit.server.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ru.practicum.shareit.server.user.dto.PatchUserRequest;
import ru.practicum.shareit.server.user.dto.PostUserRequest;
import ru.practicum.shareit.server.user.dto.UserDto;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody PostUserRequest request) {
        log.debug("server: POST /users");
        return userService.createUser(request);
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable int userId) {
        log.debug("server: GET /users/{}", userId);
        return userService.findById(userId);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable int userId,
                              @RequestBody PatchUserRequest request) {
        log.debug("server: PATCH /users/{}", userId);
        return userService.update(userId, request);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable int userId) {
        log.debug("server: DELETE /users/{}", userId);
        userService.deleteById(userId);
    }
}
