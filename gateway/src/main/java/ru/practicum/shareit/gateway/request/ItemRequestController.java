package ru.practicum.shareit.gateway.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.practicum.shareit.gateway.request.dto.ItemRequestShortDto;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createRequest(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId,
                                                @Valid @RequestBody ItemRequestShortDto request) {
        log.debug("gateway: POST /requests");
        log.debug("gateway: X-Sharer-User-Id = {}", userId);
        return itemRequestClient.createRequest(userId, request);
    }

    @GetMapping
    public ResponseEntity<Object> getUserRequests(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId) {
        log.debug("gateway: GET /requests");
        log.debug("gateway: X-Sharer-User-Id = {}", userId);
        return itemRequestClient.getAllUsersRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId) {
        log.debug("gateway: GET /requests/all");
        log.debug("gateway: X-Sharer-User-Id = {}", userId);
        return itemRequestClient.getAllRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequest(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId,
                                     @PathVariable @Positive int requestId) {
        log.debug("gateway: GET /requests/{}", requestId);
        log.debug("gateway: X-Sharer-User-Id = {}", userId);
        return itemRequestClient.getRequest(userId, requestId);
    }
}
