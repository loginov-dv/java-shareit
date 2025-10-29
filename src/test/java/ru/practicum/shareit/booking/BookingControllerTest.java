package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.PostBookingRequest;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.utils.RandomUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private BookingService bookingService;

    private final Random random = new Random();

    @Test
    void shouldCreateBooking() throws Exception {
        PostBookingRequest request = createBookingRequest();
        BookingDto savedBooking = createSavedBooking(request, random.nextInt(100), BookingStatus.WAITING);

        when(bookingService.createBooking(anyInt(), any()))
                .thenReturn(savedBooking);

        // честно говоря, все эти проверки на равенство полей
        // при использовании моков и при отсутствии логики в контроллере кажутся бессмысленными
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", savedBooking.getBooker().getId())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedBooking.getId()))
                .andExpect(jsonPath("$.status").value(savedBooking.getStatus()))
                .andExpect(jsonPath("$.start").value(request.getStart()))
                .andExpect(jsonPath("$.end").value(request.getEnd()))
                .andExpect(jsonPath("$.booker.id").value(savedBooking.getBooker().getId()))
                .andExpect(jsonPath("$.item.id").value(savedBooking.getItem().getId()));
    }

    @Test
    void shouldNotCreateBookingOfUnavailableItem() throws Exception {
        PostBookingRequest request = createBookingRequest();

        when(bookingService.createBooking(anyInt(), any()))
                .thenThrow(new NotAvailableException("Предмет недоступен для бронирования"));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateBookingForUnknownUser() throws Exception {
        PostBookingRequest request = createBookingRequest();

        when(bookingService.createBooking(anyInt(), any()))
                .thenThrow(new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, 999)));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 999)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotCreateBookingOfUnknownItem() throws Exception {
        PostBookingRequest request = createBookingRequest();

        when(bookingService.createBooking(anyInt(), any()))
                .thenThrow(new NotFoundException(String.format(ExceptionConstants.ITEM_NOT_FOUND_BY_ID, 999)));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotCreateBookingWithInvalidDates() throws Exception {
        PostBookingRequest request = createBookingRequest();

        when(bookingService.createBooking(anyInt(), any()))
                .thenThrow(new BookingDateException("Ошибка при валидации дат бронирования"));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldChangeBookingStatus(boolean approved) throws Exception {
        BookingDto bookingDto = createSavedBooking(createBookingRequest(),
                random.nextInt(100),
                approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        when(bookingService.changeBookingStatus(anyInt(), anyInt(), anyBoolean()))
                .thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/" + bookingDto.getId())
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", bookingDto.getItem().getOwnerId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(bookingDto.getStatus()));
    }

    @Test
    void shouldNotApproveBookingByWrongUser() throws Exception {
        // TODO: проверка значения поля error в возвращаемом json?
        when(bookingService.changeBookingStatus(anyInt(), anyInt(), anyBoolean()))
                .thenThrow(new NoAccessException("Нет доступа на изменение статуса бронирования"));

        mockMvc.perform(patch("/bookings/" + 1)
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetBookingById() throws Exception {
        BookingDto bookingDto = createSavedBooking(createBookingRequest(), random.nextInt(100),
                BookingStatus.APPROVED);

        when(bookingService.findById(anyInt(), anyInt()))
                .thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/" + bookingDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", bookingDto.getBooker().getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()))
                .andExpect(jsonPath("$.status").value(bookingDto.getStatus()))
                .andExpect(jsonPath("$.start").value(bookingDto.getStart()))
                .andExpect(jsonPath("$.end").value(bookingDto.getEnd()))
                .andExpect(jsonPath("$.booker.id").value(bookingDto.getBooker().getId()))
                .andExpect(jsonPath("$.item.id").value(bookingDto.getItem().getId()));
    }

    @Test
    void shouldNotGetBookingByIdIfNoAccess() throws Exception {
        when(bookingService.findById(anyInt(), anyInt()))
                .thenThrow(new NoAccessException("Нет доступа на просмотр бронирования"));

        mockMvc.perform(get("/bookings/" + 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotGetBookingByIdIfNotFoundBookingOrUser() throws Exception {
        when(bookingService.findById(anyInt(), anyInt()))
                .thenThrow(new NotFoundException("not found"));

        mockMvc.perform(get("/bookings/" + 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void shouldGetAllUsersBookingsWithGivenState(BookingState state) throws Exception {
        BookingDto booking1 = createSavedBooking(createBookingRequest(), 100, BookingStatus.WAITING);
        BookingDto booking2 = createSavedBooking(createBookingRequest(), 200, BookingStatus.APPROVED);

        when(bookingService.findAllByBookerId(anyInt(), anyString()))
                .thenReturn(List.of(booking1, booking2));

        mockMvc.perform(get("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Sharer-User-Id", 1)
                .param("state", state.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldGetAllUsersBookingsIfStateWasNotSpecified() throws Exception {
        BookingDto booking1 = createSavedBooking(createBookingRequest(), 100, BookingStatus.WAITING);
        BookingDto booking2 = createSavedBooking(createBookingRequest(), 200, BookingStatus.APPROVED);

        when(bookingService.findAllByBookerId(anyInt(), anyString()))
                .thenReturn(List.of(booking1, booking2));

        mockMvc.perform(get("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(bookingService, Mockito.times(1))
                .findAllByBookerId(1, "ALL");
    }

    @Test
    void shouldNotGetBookingsIfBookerNotFound() throws Exception {
        when(bookingService.findAllByBookerId(anyInt(), anyString()))
                .thenThrow(new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, 999)));

        mockMvc.perform(get("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void shouldGetAllOwnersBookingsWithGivenState(BookingState state) throws Exception {
        BookingDto booking1 = createSavedBooking(createBookingRequest(), 100, BookingStatus.WAITING);
        BookingDto booking2 = createSavedBooking(createBookingRequest(), 200, BookingStatus.APPROVED);

        when(bookingService.findAllByOwnerId(anyInt(), anyString()))
                .thenReturn(List.of(booking1, booking2));

        mockMvc.perform(get("/bookings/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .param("state", state.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldGetAllOwnersBookingsIfStateWasNotSpecified() throws Exception {
        BookingDto booking1 = createSavedBooking(createBookingRequest(), 100, BookingStatus.WAITING);
        BookingDto booking2 = createSavedBooking(createBookingRequest(), 200, BookingStatus.APPROVED);

        when(bookingService.findAllByOwnerId(anyInt(), anyString()))
                .thenReturn(List.of(booking1, booking2));

        mockMvc.perform(get("/bookings/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(bookingService, Mockito.times(1))
                .findAllByOwnerId(1, "ALL");
    }

    @Test
    void shouldNotGetBookingsIfOwnerNotFound() throws Exception {
        when(bookingService.findAllByOwnerId(anyInt(), anyString()))
                .thenThrow(new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, 999)));

        mockMvc.perform(get("/bookings/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestIfStateStringWasInvalid() throws Exception {
        when(bookingService.findAllByBookerId(anyInt(), anyString()))
                .thenThrow(new ArgumentException(ExceptionConstants.INVALID_BOOKING_STATE));

        mockMvc.perform(get("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());

        when(bookingService.findAllByOwnerId(anyInt(), anyString()))
                .thenThrow(new ArgumentException(ExceptionConstants.INVALID_BOOKING_STATE));

        mockMvc.perform(get("/bookings/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestIfUserHeaderIsMissing() throws Exception {
        PostBookingRequest request = createBookingRequest();

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/bookings/" + 1)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings/" + 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings/owner")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void shouldReturnBadRequestIfUserHeaderIdNotPositive(int id) throws Exception {
        PostBookingRequest request = createBookingRequest();

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Sharer-User-Id", id))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/bookings/" + 1)
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", id))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings/" + 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", id))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", id))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", id))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void shouldReturnBadRequestIfBookingIdNotPositive(int id) throws Exception {
        mockMvc.perform(patch("/bookings/" + id)
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    private PostBookingRequest createBookingRequest() {
        PostBookingRequest request = new PostBookingRequest();

        request.setItemId(random.nextInt(100));
        request.setStart(LocalDateTime.now().plusMinutes(10)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        request.setEnd(LocalDateTime.now().plusMinutes(15)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

        return request;
    }

    private BookingDto createSavedBooking(PostBookingRequest request, int bookerId, BookingStatus status) {
        BookingDto bookingDto = new BookingDto();

        bookingDto.setId(random.nextInt(100));
        bookingDto.setStart(request.getStart());
        bookingDto.setEnd(request.getEnd());
        bookingDto.setStatus(status.name());

        ItemDto itemDto = new ItemDto();
        itemDto.setId(request.getItemId());
        itemDto.setName(RandomUtils.createName());
        itemDto.setDescription(RandomUtils.createName(50));
        itemDto.setOwnerId(random.nextInt(100));
        itemDto.setAvailable(true);

        UserDto userDto = new UserDto();
        userDto.setId(bookerId);
        userDto.setName(RandomUtils.createName());
        userDto.setEmail(userDto.getName() + "@mail.ru");

        bookingDto.setItem(itemDto);
        bookingDto.setBooker(userDto);

        return bookingDto;
    }
}