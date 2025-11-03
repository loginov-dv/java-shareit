package ru.practicum.shareit.server.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.shareit.server.booking.BookingRepository;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.exception.*;
import ru.practicum.shareit.server.item.dto.ItemDetailedDto;
import ru.practicum.shareit.server.item.dto.CommentDto;
import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.item.dto.UpdateItemDto;
import ru.practicum.shareit.server.item.mapper.CommentMapper;
import ru.practicum.shareit.server.item.mapper.ItemMapper;
import ru.practicum.shareit.server.item.model.Comment;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.user.UserRepository;
import ru.practicum.shareit.server.user.model.User;

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
    public ItemDto createItem(int userId, ItemDto itemDto) {
        log.debug("Запрос на создание предмета от пользователя с id = {}: {}", userId, itemDto);

        User user = findAndGetUser(userId);
        Item item = ItemMapper.toNewItem(user, itemDto);

        item = itemRepository.save(item);
        if (itemDto.getRequestId() != null) {
            log.debug("Добавлен предмет на запрос с id = {}: {}", itemDto.getRequestId(), item);
        } else {
            log.debug("Добавлен предмет: {}", item);
        }

        return ItemMapper.toItemDto(item);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDetailedDto findById(int userId, int itemId) {
        log.debug("Запрос на получение предмета с id = {}", itemId);

        Item item = findAndGetItem(itemId);
        log.debug("Предмет: {}", item);

        Booking lastBooking = null;
        Booking nextBooking = null;

        // только для владельца
        if (item.getOwner().getId().equals(userId)) {
            List<Booking> bookings = bookingRepository.findByItemIdOrderByStart(itemId);
            Map<String, Booking> lastAndNext = getLastAndNextBooking(bookings);
            lastBooking = lastAndNext.get("last");
            nextBooking = lastAndNext.get("next");
        }

        List<Comment> comments = commentRepository.findByItemId(itemId);

        return ItemMapper.toItemDetailedDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDetailedDto> findByUserId(int userId) {
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
        Map<Integer, List<Booking>> bookingMap = bookingRepository.findByItemIdInOrderByStart(items.stream()
                .map(Item::getId).toList()).stream()
                    .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        log.debug("commentMap.size() = {}", commentMap.size());
        log.debug("bookingMap.size() = {}", bookingMap.size());

        List<ItemDetailedDto> itemDtoList = new ArrayList<>();

        for (Item item : items) {
            Map<String, Booking> lastAndNext = getLastAndNextBooking(bookingMap.get(item.getId()));

            itemDtoList.add(ItemMapper.toItemDetailedDto(item, lastAndNext.get("last"), lastAndNext.get("next"),
                    commentMap.getOrDefault(item.getId(), Collections.emptyList())));
        }

        return itemDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            log.warn("Строка для поиска была пустой");
            return Collections.emptyList();
        }

        log.debug("Запрос на поиск предметов, содержащих: {}", text);

        List<Item> items = itemRepository.search(text);

        log.debug("Количество предметов: {}", items.size());

        return items.stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    @Transactional
    public ItemDto update(int userId, int itemId, UpdateItemDto request) {
        log.debug("Запрос на обновление предмета с id = {} от пользователя с id = {}", itemId, userId);

        User user = findAndGetUser(userId);
        Item item = findAndGetItem(itemId);

        log.debug("id владельца предмета = {}", item.getOwner().getId());

        if (!item.getOwner().getId().equals(user.getId())) {
            log.warn("Нет доступа на изменение предмета");
            throw new NoAccessException("Нет доступа на изменение предмета");
        }

        log.debug("Исходное состояние предмета: {}", item);

        ItemMapper.updateItemFields(item, request);
        itemRepository.save(item);

        log.debug("Изменён предмет: {}", item);

        return ItemMapper.toItemDto(item);
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
            throw new NotAvailableException("Невозможно оставить комментарий (нет завершённой аренды)");
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

    // Вспомогательный метод для определения last и next бронирований
    private Map<String, Booking> getLastAndNextBooking(List<Booking> bookings) {
        Map<String, Booking> map = new HashMap<>();
        Booking lastBooking = null;
        Booking nextBooking = null;
        LocalDateTime now = LocalDateTime.now();

        if (bookings != null && !bookings.isEmpty()) {
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

        map.put("last", lastBooking);
        map.put("next", nextBooking);

        return map;
    }
}
