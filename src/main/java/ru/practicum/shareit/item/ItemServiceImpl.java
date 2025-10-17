package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.dto.PatchItemRequest;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemShortDto createItem(int userId, ItemShortDto itemDto) {
        log.debug("Запрос на создание предмета от пользователя с id = {}: {}", userId, itemDto);

        User user = findAndGetUser(userId);
        Item item = ItemMapper.toNewItem(user, itemDto);

        item = itemRepository.save(item);
        log.debug("Добавлен предмет: {}", item);

        return ItemMapper.toItemShortDto(item);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto findById(int userId, int itemId) {
        log.debug("Запрос на получение предмета с id = {}", itemId);

        Item item = findAndGetItem(itemId);
        log.debug("Предмет: {}", item);

        Booking lastBooking = null;
        Booking nextBooking = null;

        // только для владельца
        if (item.getOwner().getId().equals(userId)) {
            List<Booking> bookings = bookingRepository.findByItemIdOrderByStartDesc(itemId);
            LocalDateTime now = LocalDateTime.now();

            if (!bookings.isEmpty()) {
                // учитываем только завершённые и предстоящие бронирования
                List<Booking> pastBookings = bookings.stream()
                        .filter(booking -> booking.getEnd().isBefore(now))
                        .toList();
                List<Booking> futureBookings = bookings.stream()
                        .filter(booking -> booking.getStart().isAfter(now))
                        .toList();

                if (!pastBookings.isEmpty()) {
                    lastBooking = pastBookings.getLast();
                }

                if (!futureBookings.isEmpty()) {
                    nextBooking = futureBookings.getFirst();
                }
            }
        }

        List<Comment> comments = commentRepository.findByItemId(itemId);

        return ItemMapper.toItemDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemShortDto> findByUserId(int userId) {
        log.debug("Запрос на получение предметов пользователя с id = {}", userId);

        Optional<User> maybeUser = userRepository.findById(userId);

        if (maybeUser.isEmpty()) {
            log.warn(LogConstants.USER_NOT_FOUND_BY_ID, userId);
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, userId));
        }

        List<Item> items = itemRepository.findByOwnerId(userId);

        log.debug("Количество предметов: {}", items.size());

        Map<Integer, List<Comment>> commentMap = commentRepository.findByItemIdIn(items.stream()
                .map(Item::getId).toList()).stream()
                    .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        log.debug("commentMap.size() = {}", commentMap.size());

        return items.stream()
                .map(item -> ItemMapper.toItemShortDto(item,
                        commentMap.getOrDefault(item.getId(), Collections.emptyList())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemShortDto> search(String text) {
        if (text == null || text.isBlank()) {
            log.warn("Строка для поиска была пустой");
            return Collections.emptyList();
        }

        log.debug("Запрос на поиск предметов, содержащих: {}", text);

        List<Item> items = itemRepository.search(text);

        log.debug("Количество предметов: {}", items.size());

        return items.stream()
                .map(ItemMapper::toItemShortDto)
                .toList();
    }

    @Override
    @Transactional
    public ItemShortDto update(int userId, int itemId, PatchItemRequest request) {
        log.debug("Запрос на обновление предмета с id = {} от пользователя с id = {}", itemId, userId);

        User user = findAndGetUser(userId);
        Item item = findAndGetItem(itemId);

        log.debug("id владельца предмета = {}", item.getOwner().getId());

        if (!item.getOwner().getId().equals(user.getId())) {
            log.warn(LogConstants.NO_ACCESS_FOR_ITEM_UPDATE);
            throw new NoAccessException(ExceptionConstants.NO_ACCESS_FOR_ITEM_UPDATE);
        }

        log.debug("Исходное состояние предмета: {}", item);

        ItemMapper.updateItemFields(item, request);
        itemRepository.save(item);

        log.debug("Изменён предмет: {}", item);

        return ItemMapper.toItemShortDto(item);
    }

    @Override
    @Transactional
    public CommentDto createComment(int userId, int itemId, CommentDto commentDto) {
        log.debug("Запрос на создание отзыва о предмете с id = {} от пользователя с id = {}: {}",
                itemId, userId, commentDto);

        User user = findAndGetUser(userId);
        Item item = findAndGetItem(itemId);
        List<Booking> userBookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(user.getId(),
                LocalDateTime.now());

        if (userBookings.stream().noneMatch(booking -> booking.getItem().getId().equals(item.getId()))) {
            log.warn("Невозможно оставить комментарий (нет завершённой аренды)");
            throw new NotAvailableException(ExceptionConstants.HAS_NO_COMPLETED_BOOKINGS);
        }

        Comment comment = CommentMapper.toComment(user, item, commentDto);
        comment = commentRepository.save(comment);

        log.debug("Добавлен комментарий: {}", comment);

        return CommentMapper.toCommentDto(comment);
    }

    private User findAndGetUser(int userId) {
        Optional<User> maybeUser = userRepository.findById(userId);

        if (maybeUser.isEmpty()) {
            log.warn(LogConstants.USER_NOT_FOUND_BY_ID, userId);
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, userId));
        }

        return maybeUser.get();
    }

    private Item findAndGetItem(int itemId) {
        Optional<Item> maybeItem = itemRepository.findById(itemId);

        if (maybeItem.isEmpty()) {
            log.warn(LogConstants.ITEM_NOT_FOUND_BY_ID, itemId);
            throw new NotFoundException(String.format(ExceptionConstants.ITEM_NOT_FOUND_BY_ID, itemId));
        }

        return maybeItem.get();
    }
}
