package ru.practicum.shareit.gateway.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.practicum.shareit.gateway.item.dto.CommentDto;
import ru.practicum.shareit.gateway.item.dto.ItemDto;
import ru.practicum.shareit.gateway.item.dto.PatchItemRequest;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createItem(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId,
                                             @Valid @RequestBody ItemDto itemDto) {
        log.debug("gateway: POST /items");
        log.debug("gateway: X-Sharer-User-Id = {}", userId);
        return itemClient.createItem(userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId,
                                   @PathVariable @Positive int itemId) {
        log.debug("gateway: GET /items/{}", itemId);
        log.debug("gateway: X-Sharer-User-Id = {}", userId);
        return itemClient.getItem(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserItems(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId) {
        log.debug("gateway: GET /items");
        log.debug("gateway: X-Sharer-User-Id = {}", userId);
        return itemClient.getItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId,
                                @RequestParam String text) {
        log.debug("gateway: GET /items/search?text={}", text);
        log.debug("gateway: X-Sharer-User-Id = {}", userId);
        return itemClient.searchItems(userId, text);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId,
                          @PathVariable @Positive int itemId,
                          @Valid @RequestBody PatchItemRequest request) {
        log.debug("gateway: PATCH /items/{}", itemId);
        log.debug("gateway: X-Sharer-User-Id = {}", userId);
        return itemClient.updateItem(userId, itemId, request);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId,
                                    @PathVariable @Positive int itemId,
                                    @Valid @RequestBody CommentDto commentDto) {
        log.debug("gateway: POST /items/{}/comment", itemId);
        log.debug("gateway: X-Sharer-User-Id = {}", userId);
        return itemClient.createComment(userId, itemId, commentDto);
    }
}
