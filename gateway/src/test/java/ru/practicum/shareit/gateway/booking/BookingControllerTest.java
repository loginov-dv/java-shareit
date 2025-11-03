package ru.practicum.shareit.gateway.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.shareit.gateway.booking.dto.BookingDto;
import ru.practicum.shareit.gateway.booking.dto.PostBookingRequest;
import ru.practicum.shareit.gateway.booking.model.BookingState;
import ru.practicum.shareit.gateway.booking.model.BookingStatus;
import ru.practicum.shareit.gateway.utils.BookingTestData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private BookingClient bookingClient;

    private final Random random = new Random();

    @Test
    void shouldCreateBooking() throws Exception {
        PostBookingRequest request = BookingTestData.createPostBookingRequest();
        BookingDto savedBooking = BookingTestData.createBookingDto(request, BookingStatus.WAITING);

        when(bookingClient.createBooking(anyInt(), any(PostBookingRequest.class)))
                .thenReturn(new ResponseEntity<>(savedBooking, HttpStatus.CREATED));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", savedBooking.getBooker().getId())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedBooking.getId()))
                .andExpect(jsonPath("$.status").value(savedBooking.getStatus()))
                .andExpect(jsonPath("$.start").value(savedBooking.getStart()))
                .andExpect(jsonPath("$.end").value(savedBooking.getEnd()))
                .andExpect(jsonPath("$.booker.id").value(savedBooking.getBooker().getId()))
                .andExpect(jsonPath("$.item.id").value(savedBooking.getItem().getId()));
    }

    @Test
    void shouldNotCreateBookingOfUnavailableItem() throws Exception {
        PostBookingRequest request = BookingTestData.createPostBookingRequest();

        when(bookingClient.createBooking(anyInt(), any(PostBookingRequest.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateBookingForUnknownUserOrItem() throws Exception {
        PostBookingRequest request = BookingTestData.createPostBookingRequest();

        when(bookingClient.createBooking(anyInt(), any(PostBookingRequest.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 999)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidDates")
    void shouldNotCreateBookingWithInvalidDates(LocalDateTime start, LocalDateTime end) throws Exception {

        PostBookingRequest request = BookingTestData.createPostBookingRequest();
        request.setStart(start);
        request.setEnd(end);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> provideInvalidDates() {
        var now = LocalDateTime.now();

        return Stream.of(
                // start после end
                Arguments.of(now.plusHours(2), now.plusHours(1)),
                // start и end равны
                Arguments.of(now.plusHours(1), now.plusHours(1)),
                // start в прошлом
                Arguments.of(now.minusHours(1), now.plusHours(1)),
                // end в прошлом
                Arguments.of(now.plusHours(1), now.minusHours(1))
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldChangeBookingStatus(boolean approved) throws Exception {
        BookingDto bookingDto = BookingTestData.createBookingDto(BookingTestData.createPostBookingRequest(),
                approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        when(bookingClient.changeBookingStatus(anyInt(), anyInt(), anyBoolean()))
                .thenReturn(new ResponseEntity<>(bookingDto, HttpStatus.OK));

        mockMvc.perform(patch("/bookings/" + bookingDto.getId())
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", bookingDto.getItem().getOwnerId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(bookingDto.getStatus()));
    }

    @Test
    void shouldNotApproveBookingByWrongUser() throws Exception {
        when(bookingClient.changeBookingStatus(anyInt(), anyInt(), anyBoolean()))
                .thenReturn(new ResponseEntity<>(HttpStatus.FORBIDDEN));

        mockMvc.perform(patch("/bookings/" + 1)
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetBookingById() throws Exception {
        BookingDto bookingDto = BookingTestData.createBookingDto(BookingTestData.createPostBookingRequest(),
                BookingStatus.APPROVED);

        when(bookingClient.getBooking(anyInt(), anyInt()))
                .thenReturn(new ResponseEntity<>(bookingDto, HttpStatus.OK));

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
        when(bookingClient.getBooking(anyInt(), anyInt()))
                .thenReturn(new ResponseEntity<>(HttpStatus.FORBIDDEN));

        mockMvc.perform(get("/bookings/" + 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotGetBookingByIdIfNotFoundBookingOrUser() throws Exception {
        when(bookingClient.getBooking(anyInt(), anyInt()))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/bookings/" + 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void shouldGetAllUsersBookingsWithGivenState(BookingState state) throws Exception {
        BookingDto booking1 = BookingTestData.createBookingDto(BookingTestData.createPostBookingRequest(), 100,
                BookingStatus.WAITING);
        BookingDto booking2 = BookingTestData.createBookingDto(BookingTestData.createPostBookingRequest(), 200,
                BookingStatus.APPROVED);

        when(bookingClient.getAllBookersBookings(anyInt(), any(BookingState.class)))
                .thenReturn(new ResponseEntity<>(List.of(booking1, booking2), HttpStatus.OK));

        mockMvc.perform(get("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .param("state", state.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldGetAllUsersBookingsIfStateWasNotSpecified() throws Exception {
        BookingDto booking1 = BookingTestData.createBookingDto(BookingTestData.createPostBookingRequest(), 100,
                BookingStatus.WAITING);
        BookingDto booking2 = BookingTestData.createBookingDto(BookingTestData.createPostBookingRequest(), 200,
                BookingStatus.APPROVED);

        when(bookingClient.getAllBookersBookings(anyInt(), any(BookingState.class)))
                .thenReturn(new ResponseEntity<>(List.of(booking1, booking2), HttpStatus.OK));

        mockMvc.perform(get("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(bookingClient, Mockito.times(1))
                .getAllBookersBookings(1, BookingState.ALL);
    }

    @Test
    void shouldNotGetBookingsIfBookerNotFound() throws Exception {
        when(bookingClient.getAllBookersBookings(anyInt(), any(BookingState.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void shouldGetAllOwnersBookingsWithGivenState(BookingState state) throws Exception {
        BookingDto booking1 = BookingTestData.createBookingDto(BookingTestData.createPostBookingRequest(), 100,
                BookingStatus.WAITING);
        BookingDto booking2 = BookingTestData.createBookingDto(BookingTestData.createPostBookingRequest(), 200,
                BookingStatus.APPROVED);

        when(bookingClient.getAllOwnersBookings(anyInt(), any(BookingState.class)))
                .thenReturn(new ResponseEntity<>(List.of(booking1, booking2), HttpStatus.OK));

        mockMvc.perform(get("/bookings/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .param("state", state.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldGetAllOwnersBookingsIfStateWasNotSpecified() throws Exception {
        BookingDto booking1 = BookingTestData.createBookingDto(BookingTestData.createPostBookingRequest(), 100,
                BookingStatus.WAITING);
        BookingDto booking2 = BookingTestData.createBookingDto(BookingTestData.createPostBookingRequest(), 200,
                BookingStatus.APPROVED);

        when(bookingClient.getAllOwnersBookings(anyInt(), any(BookingState.class)))
                .thenReturn(new ResponseEntity<>(List.of(booking1, booking2), HttpStatus.OK));

        mockMvc.perform(get("/bookings/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(bookingClient, Mockito.times(1))
                .getAllOwnersBookings(1, BookingState.ALL);
    }

    @Test
    void shouldNotGetBookingsIfOwnerNotFound() throws Exception {
        when(bookingClient.getAllOwnersBookings(anyInt(), any(BookingState.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/bookings/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestIfStateStringWasInvalid() throws Exception {
        when(bookingClient.getAllBookersBookings(anyInt(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mockMvc.perform(get("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());

        when(bookingClient.getAllOwnersBookings(anyInt(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mockMvc.perform(get("/bookings/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestIfUserHeaderIsMissing() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(BookingTestData.createPostBookingRequest())))
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
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(BookingTestData.createPostBookingRequest()))
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
}