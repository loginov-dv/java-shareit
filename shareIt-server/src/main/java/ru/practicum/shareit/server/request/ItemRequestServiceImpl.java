package ru.practicum.shareit.server.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.shareit.server.exception.ExceptionConstants;
import ru.practicum.shareit.server.exception.LogConstants;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.item.ItemRepository;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.request.dto.ItemRequestDto;
import ru.practicum.shareit.server.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.server.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.user.UserRepository;
import ru.practicum.shareit.server.user.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestShortDto createRequest(int userId, ItemRequestShortDto dto) {
        log.debug("Создание нового запроса вещи от пользователя с id = {}: {}", userId, dto);

        User user = findAndGetUser(userId);
        ItemRequest request = ItemRequestMapper.toNewItemRequest(user, dto);
        request = itemRequestRepository.save(request);

        log.debug("Добавлен запрос: {}", request);

        return ItemRequestMapper.toItemRequestShortDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> findByUserId(int userId) {
        log.debug("Получение всех запросов пользователя с id = {}", userId);

        findAndGetUser(userId);
        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId);
        Map<Integer, List<Item>> itemsMap = itemRepository.findByRequestIdIn(requests.stream()
                        .map(ItemRequest::getId)
                        .toList())
                .stream()
                .collect(Collectors.groupingBy(Item::getRequestId));

        log.debug("itemsMap.size() = {}", itemsMap.size());

        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestDto(request,
                        itemsMap.getOrDefault(request.getId(), Collections.emptyList()))).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestShortDto> findAll(int userId) {
        log.debug("Получение всех чужих запросов для пользователя с id = {}", userId);

        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(userId);

        return requests.stream()
                .map(ItemRequestMapper::toItemRequestShortDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestDto findById(int requestId) {
        log.debug("Получение запроса с id = {}", requestId);

        Optional<ItemRequest> maybeRequest = itemRequestRepository.findById(requestId);

        if (maybeRequest.isEmpty()) {
            log.warn("Запрос с id = {} не найден", requestId);
            throw new NotFoundException("Запрос с id = " + requestId + " не найден");
        }

        List<Item> items = itemRepository.findByRequestId(requestId);

        log.debug("items.size() = {}", items.size());

        return ItemRequestMapper.toItemRequestDto(maybeRequest.get(), items);
    }

    @Transactional(readOnly = true)
    private User findAndGetUser(int userId) {
        Optional<User> maybeUser = userRepository.findById(userId);

        if (maybeUser.isEmpty()) {
            log.warn(LogConstants.USER_NOT_FOUND_BY_ID, userId);
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, userId));
        }

        return maybeUser.get();
    }
}
