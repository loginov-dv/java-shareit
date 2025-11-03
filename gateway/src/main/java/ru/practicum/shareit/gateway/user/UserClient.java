package ru.practicum.shareit.gateway.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import ru.practicum.shareit.gateway.client.BaseClient;
import ru.practicum.shareit.gateway.user.dto.UpdateUserDto;
import ru.practicum.shareit.gateway.user.dto.NewUserDto;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> createUser(NewUserDto request) {
        return post("", request);
    }

    public ResponseEntity<Object> getUser(int userId) {
        return get("/" + userId);
    }

    public ResponseEntity<Object> updateUser(int userId, UpdateUserDto request) {
        return patch("/" + userId, request);
    }

    public ResponseEntity<Object> deleteUser(int userId) {
        return delete("/" + userId);
    }
}
