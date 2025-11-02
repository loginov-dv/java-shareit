package ru.practicum.shareit.server.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ru.practicum.shareit.server.item.dto.CommentDto;
import ru.practicum.shareit.server.item.dto.ItemDetailedDto;
import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.item.dto.PatchItemRequest;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Integer userId,
                              @RequestBody ItemDto itemDto) {
        log.debug("server: POST /items");
        log.debug("server: X-Sharer-User-Id = {}", userId);
        return itemService.createItem(userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDetailedDto getItem(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                   @PathVariable int itemId) {
        log.debug("server: GET /items/{}", itemId);
        log.debug("server: X-Sharer-User-Id = {}", userId);
        return itemService.findById(userId, itemId);
    }

    @GetMapping
    public List<ItemDetailedDto> getUserItems(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.debug("server: GET /items");
        log.debug("server: X-Sharer-User-Id = {}", userId);
        return itemService.findByUserId(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                @RequestParam String text) {
        log.debug("server: GET /items/search?text={}", text);
        log.debug("server: X-Sharer-User-Id = {}", userId);
        return itemService.search(text);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Integer userId,
                          @PathVariable int itemId,
                          @RequestBody PatchItemRequest request) {
        log.debug("server: PATCH /items/{}", itemId);
        log.debug("server: X-Sharer-User-Id = {}", userId);
        return itemService.update(userId, itemId, request);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                    @PathVariable int itemId,
                                    @RequestBody CommentDto commentDto) {
        log.debug("server: POST /items/{}/comment", itemId);
        log.debug("server: X-Sharer-User-Id = {}", userId);
        return itemService.createComment(userId, itemId, commentDto);
    }
}
