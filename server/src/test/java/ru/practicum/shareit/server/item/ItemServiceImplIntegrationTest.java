package ru.practicum.shareit.server.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.booking.BookingService;
import ru.practicum.shareit.server.booking.BookingServiceImpl;
import ru.practicum.shareit.server.booking.dto.BookingDto;
import ru.practicum.shareit.server.item.dto.CommentDto;
import ru.practicum.shareit.server.item.dto.ItemDetailedDto;
import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.user.UserService;
import ru.practicum.shareit.server.user.UserServiceImpl;
import ru.practicum.shareit.server.user.dto.UserDto;
import ru.practicum.shareit.server.utils.BookingTestData;
import ru.practicum.shareit.server.utils.ItemTestData;
import ru.practicum.shareit.server.utils.UserTestData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@Transactional
@DataJpaTest
@Import(value = {ItemServiceImpl.class, UserServiceImpl.class, BookingServiceImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"/schema.sql", "/clear.sql"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplIntegrationTest {
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;

    @Test
    void shouldFindItemsByUserId() {
        // добавим три пользователя
        UserDto user1 = userService.createUser(UserTestData.createNewUserDto());
        UserDto user2 = userService.createUser(UserTestData.createNewUserDto());
        UserDto user3 = userService.createUser(UserTestData.createNewUserDto());

        // у пользователя user1 будет два предмета, у остальных - по одному
        ItemDto item11 = itemService.createItem(user1.getId(), ItemTestData.createNewItemDto());
        ItemDto item12 = itemService.createItem(user1.getId(), ItemTestData.createNewItemDto());
        ItemDto item21 = itemService.createItem(user2.getId(), ItemTestData.createNewItemDto());
        ItemDto item31 = itemService.createItem(user3.getId(), ItemTestData.createNewItemDto());
        List<ItemDto> items = List.of(item11, item12, item21, item31);

        // для каждого предмета добавим по два бронирования - завершённое и предстоящее,
        // а также один комментарий (для завершённого бронирования)
        for (ItemDto itemDto : items) {
            UserDto bookerCompleted = userService.createUser(UserTestData.createNewUserDto());
            BookingDto completedBooking = bookingService.createBooking(bookerCompleted.getId(),
                    BookingTestData.createNewBookingDto(itemDto, true));

            UserDto bookerFuture = userService.createUser(UserTestData.createNewUserDto());
            BookingDto futureBooking = bookingService.createBooking(bookerFuture.getId(),
                    BookingTestData.createNewBookingDto(itemDto, false));

            CommentDto commentCompleted = itemService.createComment(bookerCompleted.getId(), itemDto.getId(),
                    ItemTestData.createNewCommentDto());
        }

        // получим все предметы пользователя user1
        List<ItemDetailedDto> userItems = itemService.findByUserId(user1.getId());

        assertEquals(2, userItems.size());
        for (ItemDetailedDto itemDetailedDto : userItems) {
            assertNotNull(itemDetailedDto.getLastBooking());
            assertNotNull(itemDetailedDto.getNextBooking());
            assertNotNull(itemDetailedDto.getComments());
            assertEquals(1, itemDetailedDto.getComments().size());
            assertEquals(itemDetailedDto.getId(), itemDetailedDto.getComments().getFirst().getItemId());
        }
    }
}