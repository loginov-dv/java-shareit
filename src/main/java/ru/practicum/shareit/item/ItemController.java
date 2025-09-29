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
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") @NotNull @Positive Integer userId,
                          @Valid @RequestBody ItemDto itemDto) {
        log.debug("POST /items");
        return itemService.add(userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@PathVariable @Positive int itemId) {
        log.debug("GET /items/{}", itemId);
        return itemService.findById(itemId);
    }

    @GetMapping
    public List<ItemDto> getByUser(@RequestHeader("X-Sharer-User-Id") @NotNull @Positive Integer userId) {
        log.debug("GET /items");
        return itemService.findByUser(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        log.debug("GET /items/search?text={}", text);
        return itemService.search(text);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") @NotNull @Positive Integer userId,
                       @PathVariable @Positive int itemId,
                       @Valid @RequestBody PatchItemRequest request) {
        log.debug("PATCH /items/{}", itemId);
        return itemService.update(userId, itemId, request);
    }
}
