package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ExceptionConstants;
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
    public ItemDto add(int userId, ItemDto itemDto) {
        Optional<User> maybeUser = userRepository.findById(userId);

        if (maybeUser.isEmpty()) {
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, userId));
        }

        Item item = ItemMapper.toItem(userId, itemDto);
        item = itemRepository.save(item);

        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto findById(int itemId) {
        Optional<Item> maybeItem = itemRepository.findById(itemId);

        if (maybeItem.isEmpty()) {
            throw new NotFoundException(String.format(ExceptionConstants.ITEM_NOT_FOUND_BY_ID, itemId));
        }

        return ItemMapper.toItemDto(maybeItem.get());
    }

    @Override
    public List<ItemDto> findByUser(int userId) {
        Optional<User> maybeUser = userRepository.findById(userId);

        if (maybeUser.isEmpty()) {
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, userId));
        }

        List<Item> items = itemRepository.findByUserId(userId);

        return items.stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public ItemDto update(int userId, int itemId, PatchItemRequest request) {
        Optional<User> maybeUser = userRepository.findById(userId);

        if (maybeUser.isEmpty()) {
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, userId));
        }

        Optional<Item> maybeItem = itemRepository.findById(itemId);

        if (maybeItem.isEmpty()) {
            throw new NotFoundException(String.format(ExceptionConstants.ITEM_NOT_FOUND_BY_ID, itemId));
        }

        User user = maybeUser.get();
        Item item = maybeItem.get();

        if (!item.getOwnerId().equals(user.getId())) {
            throw new NoAccessException(ExceptionConstants.NO_ACCESS_FOR_EDIT);
        }

        ItemMapper.updateItemFields(item, request);
        itemRepository.update(item);

        return ItemMapper.toItemDto(item);
    }
}
