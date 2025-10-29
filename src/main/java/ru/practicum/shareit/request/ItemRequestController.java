package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestShortDto createRequest(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId,
                                             @Valid @RequestBody ItemRequestShortDto itemRequestDto) {
        log.debug("POST /requests");
        log.debug("X-Sharer-User-Id = {}", userId);
        return itemRequestService.createRequest(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getUserRequests(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId) {
        log.debug("GET /requests");
        log.debug("X-Sharer-User-Id = {}", userId);
        return itemRequestService.findByUserId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestShortDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId) {
        log.debug("GET /requests/all");
        log.debug("X-Sharer-User-Id = {}", userId);
        return itemRequestService.findAll(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequest(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId,
                                     @PathVariable @Positive int requestId) {
        log.debug("GET /requests/{}", requestId);
        log.debug("X-Sharer-User-Id = {}", userId);
        return itemRequestService.findById(requestId);
    }
}
