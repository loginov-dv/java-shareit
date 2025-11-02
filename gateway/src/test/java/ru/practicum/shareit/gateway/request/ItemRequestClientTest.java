package ru.practicum.shareit.gateway.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import ru.practicum.shareit.gateway.request.dto.ItemRequestDto;
import ru.practicum.shareit.gateway.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.gateway.utils.ItemRequestTestData;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestClientTest {
    @Mock
    private RestTemplate restTemplate;

    private ItemRequestClient requestClient;

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
        requestClient = new ItemRequestClient(url, builder);
    }

    @Test
    void shouldCreateRequest() {
        ItemRequestShortDto request = ItemRequestTestData.createNewItemRequest();
        ItemRequestShortDto savedRequest = ItemRequestTestData.createItemRequestShortDto();
        savedRequest.setRequestorId(request.getRequestorId());

        when(restTemplate.exchange(
                eq(""),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(new ResponseEntity<>(savedRequest, HttpStatus.CREATED));

        ResponseEntity<Object> actualResponse = requestClient.createRequest(request.getRequestorId(), request);

        assertNotNull(actualResponse);
        assertEquals(HttpStatus.CREATED, actualResponse.getStatusCode());
        assertEquals(savedRequest, actualResponse.getBody());
    }

    @Test
    void shouldGetAllUsersRequests() {
        ItemRequestDto request1 = ItemRequestTestData.createItemRequestDto();
        ItemRequestDto request2 = ItemRequestTestData.createItemRequestDto();
        request2.setRequestorId(request1.getRequestorId());

        when(restTemplate.exchange(
                eq(""),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(ResponseEntity.ok(List.of(request1, request2)));

        ResponseEntity<Object> actualResponse = requestClient.getAllUsersRequests(request1.getRequestorId());

        assertNotNull(actualResponse);
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(List.of(request1, request2), actualResponse.getBody());
    }

    @Test
    void shouldGetAllRequests() {
        ItemRequestDto request1 = ItemRequestTestData.createItemRequestDto();
        ItemRequestDto request2 = ItemRequestTestData.createItemRequestDto();

        when(restTemplate.exchange(
                eq("/all"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(ResponseEntity.ok(List.of(request1, request2)));

        ResponseEntity<Object> actualResponse = requestClient.getAllRequests(1);

        assertNotNull(actualResponse);
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(List.of(request1, request2), actualResponse.getBody());
    }

    @Test
    void shouldGetRequest() {
        ItemRequestDto request = ItemRequestTestData.createItemRequestDto();

        when(restTemplate.exchange(
                eq("/" + request.getId()),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(ResponseEntity.ok(request));

        ResponseEntity<Object> actualResponse = requestClient.getRequest(1, request.getId());

        assertNotNull(actualResponse);
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(request, actualResponse.getBody());
    }
}