package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ExceptionConstants;
import ru.practicum.shareit.exception.LogConstants;
import ru.practicum.shareit.exception.NoAccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.PatchItemRequest;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto createItem(int userId, ItemDto itemDto) {
        Optional<User> maybeUser = userRepository.findById(userId);

        if (maybeUser.isEmpty()) {
            log.warn(LogConstants.USER_NOT_FOUND_BY_ID, userId);
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, userId));
        }

        Item item = ItemMapper.toItem(maybeUser.get(), itemDto);
        item = itemRepository.save(item);

        log.debug("Добавлен предмет: {}", item);

        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto findById(int itemId) {
        Optional<Item> maybeItem = itemRepository.findById(itemId);

        if (maybeItem.isEmpty()) {
            log.warn(LogConstants.ITEM_NOT_FOUND_BY_ID, itemId);
            throw new NotFoundException(String.format(ExceptionConstants.ITEM_NOT_FOUND_BY_ID, itemId));
        }

        log.debug("Найден предмет по id = {}: {}", itemId, maybeItem.get());

        return ItemMapper.toItemDto(maybeItem.get());
    }

    @Override
    public List<ItemDto> findByUserId(int userId) {
        Optional<User> maybeUser = userRepository.findById(userId);

        if (maybeUser.isEmpty()) {
            log.warn(LogConstants.USER_NOT_FOUND_BY_ID, userId);
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, userId));
        }

        List<Item> items = itemRepository.findByOwnerId(userId);

        log.debug("Найдены предметы по userId = {}: {}", userId, items);

        return items.stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        List<Item> items = itemRepository.search(text);

        log.debug("Предметы, найденные по строке {}: {}", text, items);

        return items.stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public ItemDto update(int userId, int itemId, PatchItemRequest request) {
        Optional<User> maybeUser = userRepository.findById(userId);

        if (maybeUser.isEmpty()) {
            log.warn(LogConstants.USER_NOT_FOUND_BY_ID, userId);
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, userId));
        }

        Optional<Item> maybeItem = itemRepository.findById(itemId);

        if (maybeItem.isEmpty()) {
            log.warn(LogConstants.ITEM_NOT_FOUND_BY_ID, itemId);
            throw new NotFoundException(String.format(ExceptionConstants.ITEM_NOT_FOUND_BY_ID, itemId));
        }

        User user = maybeUser.get();
        Item item = maybeItem.get();

        log.debug("Запрос на изменение предмета с id = {} (id владельца = {}) пользователем с id = {}",
                item.getId(), item.getOwner().getId(), user.getId());

        if (!item.getOwner().getId().equals(user.getId())) {
            log.warn("Нет доступа на изменение предмета");
            throw new NoAccessException(ExceptionConstants.NO_ACCESS_FOR_EDIT);
        }

        log.debug("Исходное состояние предмета: {}", item);

        ItemMapper.updateItemFields(item, request);
        itemRepository.save(item);

        log.debug("Изменён предмет: {}", item);

        return ItemMapper.toItemDto(item);
    }
}
