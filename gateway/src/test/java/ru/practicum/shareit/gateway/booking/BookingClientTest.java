package ru.practicum.shareit.gateway.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;
import ru.practicum.shareit.gateway.booking.dto.BookingDto;
import ru.practicum.shareit.gateway.booking.dto.NewBookingDto;
import ru.practicum.shareit.gateway.booking.model.BookingState;
import ru.practicum.shareit.gateway.booking.model.BookingStatus;
import ru.practicum.shareit.gateway.user.dto.UserDto;
import ru.practicum.shareit.gateway.utils.BookingTestData;
import ru.practicum.shareit.gateway.utils.UserTestData;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingClientTest {
    @Mock
    private RestTemplate restTemplate;

    private BookingClient bookingClient;

    @BeforeEach
    void setUp() {
        // Создаем RestTemplateBuilder который полностью возвращает наш мок
        RestTemplateBuilder builder = new RestTemplateBuilder() {
            @Override
            public RestTemplateBuilder uriTemplateHandler(UriTemplateHandler handler) {
                // Ничего не делаем, просто возвращаем this
                return this;
            }

            @Override
            public RestTemplateBuilder requestFactory(Supplier<ClientHttpRequestFactory> requestFactorySupplier) {
                // Ничего не делаем, просто возвращаем this
                return this;
            }

            @Override
            public RestTemplate build() {
                return restTemplate;
            }
        };

        String url = "http://localhost:9090/users";
        bookingClient = new BookingClient(url, builder);
    }

    @Test
    void shouldCreateBooking() {
        NewBookingDto request = BookingTestData.createNewBookingDto();
        UserDto booker = UserTestData.createUserDto();
        BookingDto savedBooking = BookingTestData.createBookingDto(request, booker.getId(), BookingStatus.WAITING);

        when(restTemplate.exchange(
                eq(""),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(new ResponseEntity<>(savedBooking, HttpStatus.CREATED));

        ResponseEntity<Object> actualResponse = bookingClient.createBooking(booker.getId(), request);

        assertNotNull(actualResponse);
        assertEquals(HttpStatus.CREATED, actualResponse.getStatusCode());
        assertEquals(savedBooking, actualResponse.getBody());
    }

    @Test
    void shouldGetBooking() {
        BookingDto booking = BookingTestData.createBookingDto(BookingStatus.WAITING);

        when(restTemplate.exchange(
                eq("/" + booking.getId()),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(ResponseEntity.ok(booking));

        ResponseEntity<Object> actualResponse = bookingClient.getBooking(1, booking.getId());

        assertNotNull(actualResponse);
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(booking, actualResponse.getBody());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldChangeBookingStatus(boolean approved) {
        BookingDto booking = BookingTestData.createBookingDto(BookingStatus.WAITING);
        booking.setStatus(approved ? BookingStatus.APPROVED.name() : BookingStatus.REJECTED.name());

        when(restTemplate.exchange(
                eq("/" + booking.getId() + "?approved={approved}"),
                eq(HttpMethod.PATCH),
                any(HttpEntity.class),
                eq(Object.class),
                eq(Map.of("approved", approved))
        )).thenReturn(ResponseEntity.ok(booking));

        ResponseEntity<Object> actualResponse = bookingClient.changeBookingStatus(1, booking.getId(), approved);

        assertNotNull(actualResponse);
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(booking, actualResponse.getBody());
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void shouldGetAllBookersBookings(BookingState state) {
        BookingDto booking1 = BookingTestData.createBookingDto(BookingStatus.APPROVED);
        BookingDto booking2 = BookingTestData.createBookingDto(BookingStatus.WAITING);
        booking2.setBooker(booking1.getBooker());

        when(restTemplate.exchange(
                eq("?state={state}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                eq(Map.of("state", state.name()))
        )).thenReturn(ResponseEntity.ok(List.of(booking1, booking2)));

        ResponseEntity<Object> actualResponse = bookingClient.getAllBookersBookings(booking1.getBooker().getId(), state);

        assertNotNull(actualResponse);
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(List.of(booking1, booking2), actualResponse.getBody());
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void shouldGetAllOwnersBookings(BookingState state) {
        BookingDto booking1 = BookingTestData.createBookingDto(BookingStatus.APPROVED);
        BookingDto booking2 = BookingTestData.createBookingDto(BookingStatus.WAITING);
        booking2.getItem().setOwnerId(booking1.getItem().getOwnerId());

        when(restTemplate.exchange(
                eq("/owner?state={state}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                eq(Map.of("state", state.name()))
        )).thenReturn(ResponseEntity.ok(List.of(booking1, booking2)));

        ResponseEntity<Object> actualResponse = bookingClient.getAllOwnersBookings(booking1.getBooker().getId(), state);

        assertNotNull(actualResponse);
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(List.of(booking1, booking2), actualResponse.getBody());
    }
}