package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.dto.ItemOwnerDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
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

// TODO: logs
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

        Optional<User> maybeUser = userRepository.findById(userId);

        if (maybeUser.isEmpty()) {
            log.warn(LogConstants.USER_NOT_FOUND_BY_ID, userId);
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, userId));
        }

        Item item = ItemMapper.toNewItem(maybeUser.get(), itemDto);

        item = itemRepository.save(item);
        log.debug("Добавлен предмет: {}", item);

        return ItemMapper.toItemDto(item);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto findById(int itemId) {
        log.debug("Запрос на получение предмета с id = {}", itemId);

        Optional<Item> maybeItem = itemRepository.findById(itemId);

        if (maybeItem.isEmpty()) {
            log.warn(LogConstants.ITEM_NOT_FOUND_BY_ID, itemId);
            throw new NotFoundException(String.format(ExceptionConstants.ITEM_NOT_FOUND_BY_ID, itemId));
        }

        log.debug("Предмет: {}", maybeItem.get());

        List<Comment> comments = commentRepository.findByItemId(itemId);

        return ItemMapper.toItemDto(maybeItem.get(), comments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemOwnerDto> findByUserId(int userId) {
        log.debug("Запрос на получение предметов пользователя с id = {}", userId);

        Optional<User> maybeUser = userRepository.findById(userId);

        if (maybeUser.isEmpty()) {
            log.warn(LogConstants.USER_NOT_FOUND_BY_ID, userId);
            throw new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, userId));
        }

        List<Item> items = itemRepository.findByOwnerId(userId);

        Map<Integer, List<Booking>> bookingMap = bookingRepository.findByItemIdInOrderByStartDesc(items.stream()
                .map(Item::getId)
                .toList()).stream()
                    .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        log.debug("bookingMap.size() = {}", bookingMap.size());

        Map<Integer, List<Comment>> commentMap = commentRepository.findByItemIdIn(items.stream()
                .map(Item::getId)
                .toList()).stream()
                    .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        log.debug("commentMap.size() = {}", commentMap.size());

        List<ItemOwnerDto> dtos = new ArrayList<>();

        for (Item item : items) {
            List<Booking> bookings = bookingMap.get(item.getId());

            Booking lastBooking = null;
            Booking nextBooking = null;
            //Instant now = Instant.now();
            LocalDateTime now = LocalDateTime.now();

            if (bookings != null) {
                // текущее бронирование не учитываем
                try {
                    lastBooking = bookings.stream()
                            .filter(booking -> booking.getEnd().isBefore(now))
                            .toList().getLast();

                    nextBooking = bookings.stream()
                            .filter(booking -> booking.getStart().isAfter(now))
                            .toList().getFirst();
                } catch (NoSuchElementException ignored) {
                    log.debug("no such element");
                }
            }

            // TODO: null
            List<Comment> comments = commentMap.get(item.getId());

            dtos.add(ItemMapper.toItemOwnerDto(item, lastBooking, nextBooking, comments));
        }

        log.debug("Количество предметов: {}", dtos.size());

        /*return items.stream()
                .map(ItemMapper::toItemDto)
                .toList();*/
        return dtos;
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
    public ItemDto update(int userId, int itemId, PatchItemRequest request) {
        log.debug("Запрос на обновление предмета с id = {} от пользователя с id = {}", itemId, userId);

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

        log.debug("id владельца предмета = {}", item.getOwner().getId());

        if (!item.getOwner().getId().equals(user.getId())) {
            log.warn("Нет доступа на изменение предмета");
            throw new NoAccessException(ExceptionConstants.NO_ACCESS);
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
        // TODO: получение с проверкой и выбрасыванием исключения повторяется многократно, мб вынести в метод
        log.debug("Запрос на создание комментария от пользователя с id = {} на предмет с id = {}: {}",
                userId, itemId, commentDto);

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

        List<Booking> userBookings = bookingRepository.findPastByBookerIdAndEndIsBefore(maybeUser.get().getId(),
                LocalDateTime.now());

        // TODO: ex
        if (userBookings.stream().noneMatch(booking -> booking.getItem().getId().equals(maybeItem.get().getId()))) {
            log.warn("Невозможно оставить комментарий (нет завершённой аренды)");
            throw new NotAvailableException(ExceptionConstants.HAS_NO_COMPLETED_BOOKINGS);
        }

        Comment comment = CommentMapper.toComment(maybeUser.get(), maybeItem.get(), commentDto);
        comment = commentRepository.save(comment);

        log.debug("Добавлен комментарий: {}", comment);

        return CommentMapper.toCommentDto(comment);
    }
}
