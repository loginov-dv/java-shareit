package ru.practicum.shareit.gateway.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;
import ru.practicum.shareit.gateway.user.dto.PatchUserRequest;
import ru.practicum.shareit.gateway.user.dto.PostUserRequest;
import ru.practicum.shareit.gateway.user.dto.UserDto;
import ru.practicum.shareit.gateway.utils.UserTestData;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserClientTest {
    @Mock
    private RestTemplate restTemplate;

    private UserClient userClient;

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
        userClient = new UserClient(url, builder);
    }

    @Test
    void shouldCreateUser() {
        PostUserRequest request = UserTestData.createPostUserRequest();
        UserDto body = UserTestData.createUserDto(request);

        when(restTemplate.exchange(
                eq(""),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(new ResponseEntity<>(body, HttpStatus.CREATED));

        ResponseEntity<Object> actualResponse = userClient.createUser(request);

        assertNotNull(actualResponse);
        assertEquals(HttpStatus.CREATED, actualResponse.getStatusCode());
        assertEquals(body, actualResponse.getBody());
    }

    @Test
    void shouldGetUser() {
        UserDto user = UserTestData.createUserDto();

        when(restTemplate.exchange(
                eq("/" + user.getId()),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(ResponseEntity.ok(user));

        ResponseEntity<Object> actualResponse = userClient.getUser(user.getId());

        assertNotNull(actualResponse);
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(user, actualResponse.getBody());
    }

    @Test
    void shouldUpdateUser() {
        PatchUserRequest request = UserTestData.createPatchUserRequest();
        UserDto updatedUser = new UserDto();
        updatedUser.setId(1);
        updatedUser.setName(request.getName());
        updatedUser.setEmail(request.getEmail());

        when(restTemplate.exchange(
                eq("/" + updatedUser.getId()),
                eq(HttpMethod.PATCH),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(ResponseEntity.ok(updatedUser));

        ResponseEntity<Object> actualResponse = userClient.updateUser(updatedUser.getId(), request);

        assertNotNull(actualResponse);
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(updatedUser, actualResponse.getBody());
    }

    @Test
    void shouldDeleteUser() {
        UserDto user = UserTestData.createUserDto();

        when(restTemplate.exchange(
                eq("/" + user.getId()),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ResponseEntity<Object> actualResponse = userClient.deleteUser(user.getId());

        assertNotNull(actualResponse);
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertFalse(actualResponse.hasBody());
    }

    private HttpHeaders defaultHeaders(Integer userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId != null) {
            headers.set("X-Sharer-User-Id", String.valueOf(userId));
        }
        return headers;
    }
}