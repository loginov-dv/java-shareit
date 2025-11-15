package ru.practicum.shareit.server.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ru.practicum.shareit.server.request.dto.ItemRequestDto;
import ru.practicum.shareit.server.request.dto.ItemRequestShortDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestShortDto createRequest(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                             @RequestBody ItemRequestShortDto itemRequestDto) {
        log.debug("server: POST /requests");
        log.debug("server: X-Sharer-User-Id = {}", userId);
        return itemRequestService.createRequest(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getUserRequests(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.debug("server: GET /requests");
        log.debug("server: X-Sharer-User-Id = {}", userId);
        return itemRequestService.findByUserId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestShortDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.debug("server: GET /requests/all");
        log.debug("server: X-Sharer-User-Id = {}", userId);
        return itemRequestService.findAll(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequest(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                     @PathVariable int requestId) {
        log.debug("server: GET /requests/{}", requestId);
        log.debug("server: X-Sharer-User-Id = {}", userId);
        return itemRequestService.findById(requestId);
    }
}