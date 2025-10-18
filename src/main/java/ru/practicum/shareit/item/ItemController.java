package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.dto.PatchItemRequest;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemShortDto createItem(@RequestHeader("X-Sharer-User-Id") @NotNull @Positive Integer userId,
                                   @Valid @RequestBody ItemShortDto itemDto) {
        log.debug("POST /items");
        log.debug("X-Sharer-User-Id = {}", userId);
        return itemService.createItem(userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@RequestHeader("X-Sharer-User-Id") @NotNull @Positive Integer userId,
                           @PathVariable @Positive int itemId) {
        log.debug("GET /items/{}", itemId);
        log.debug("X-Sharer-User-Id = {}", userId);
        return itemService.findById(userId, itemId);
    }

    @GetMapping
    public List<ItemDto> getUserItems(@RequestHeader("X-Sharer-User-Id") @NotNull @Positive Integer userId) {
        log.debug("GET /items");
        log.debug("X-Sharer-User-Id = {}", userId);
        return itemService.findByUserId(userId);
    }

    @GetMapping("/search")
    public List<ItemShortDto> search(@RequestHeader("X-Sharer-User-Id") @NotNull @Positive Integer userId,
                                     @RequestParam String text) {
        log.debug("GET /items/search?text={}", text);
        log.debug("X-Sharer-User-Id = {}", userId);
        return itemService.search(text);
    }

    @PatchMapping("/{itemId}")
    public ItemShortDto update(@RequestHeader("X-Sharer-User-Id") @NotNull @Positive Integer userId,
                               @PathVariable @Positive int itemId,
                               @Valid @RequestBody PatchItemRequest request) {
        log.debug("PATCH /items/{}", itemId);
        log.debug("X-Sharer-User-Id = {}", userId);
        return itemService.update(userId, itemId, request);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") @NotNull @Positive Integer userId,
                                    @PathVariable @Positive int itemId,
                                    @Valid @RequestBody CommentDto commentDto) {
        log.debug("POST /items/{}/comment", itemId);
        log.debug("X-Sharer-User-Id = {}", userId);
        return itemService.createComment(userId, itemId, commentDto);
    }
}
