package ru.practicum.shareit.gateway.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.gateway.booking.dto.NewBookingDto;
import ru.practicum.shareit.gateway.booking.model.BookingState;
import ru.practicum.shareit.gateway.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> createBooking(int bookerId, NewBookingDto request) {
        return post("", bookerId, request);
    }

    public ResponseEntity<Object> changeBookingStatus(int userId, int bookingId, boolean approved) {
        Map<String, Object> params = Map.of(
                "approved", approved
        );

        return patch("/" + bookingId + "?approved={approved}", userId, params, null);
    }

    public ResponseEntity<Object> getBooking(int userId, int bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getAllBookersBookings(int userId, BookingState state) {
        Map<String, Object> params = Map.of(
                "state", state.name()
        );

        return get("?state={state}", userId, params);
    }

    public ResponseEntity<Object> getAllOwnersBookings(int userId, BookingState state) {
        Map<String, Object> params = Map.of(
                "state", state.name()
        );

        return get("/owner?state={state}", userId, params);
    }
}
