package ru.practicum.shareit.gateway.item;

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
import ru.practicum.shareit.gateway.item.dto.CommentDto;
import ru.practicum.shareit.gateway.item.dto.ItemDto;
import ru.practicum.shareit.gateway.item.dto.PatchItemRequest;
import ru.practicum.shareit.gateway.user.dto.UserDto;
import ru.practicum.shareit.gateway.utils.ItemTestData;
import ru.practicum.shareit.gateway.utils.UserTestData;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemClientTest {
    @Mock
    private RestTemplate restTemplate;

    private ItemClient itemClient;

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
        itemClient = new ItemClient(url, builder);
    }

    @Test
    void shouldCreateItem() {
        ItemDto request = ItemTestData.createNewItemDto();
        ItemDto savedItem = ItemTestData.createItemDto(request);
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>(savedItem, HttpStatus.CREATED);

        when(restTemplate.exchange(
                eq(""),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(new ResponseEntity<>(savedItem, HttpStatus.CREATED));

        ResponseEntity<Object> actualResponse = itemClient.createItem(savedItem.getOwnerId(), request);

        assertNotNull(actualResponse);
        assertEquals(HttpStatus.CREATED, actualResponse.getStatusCode());
        assertEquals(savedItem, actualResponse.getBody());
    }

    @Test
    void shouldGetItem() {
        ItemDto item = ItemTestData.createItemDto();

        when(restTemplate.exchange(
                eq("/" + item.getId()),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(ResponseEntity.ok(item));

        ResponseEntity<Object> actualResponse = itemClient.getItem(item.getOwnerId(), item.getId());

        assertNotNull(actualResponse);
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(item, actualResponse.getBody());
    }

    @Test
    void shouldGetItems() {
        ItemDto item1 = ItemTestData.createItemDto();
        ItemDto item2 = ItemTestData.createItemDto();
        item2.setOwnerId(item1.getOwnerId());

        when(restTemplate.exchange(
                eq(""),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(ResponseEntity.ok(List.of(item1, item2)));

        ResponseEntity<Object> actualResponse = itemClient.getItems(item1.getOwnerId());

        assertNotNull(actualResponse);
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(List.of(item1, item2), actualResponse.getBody());
    }

    @Test
    void shouldSearchItems() {
        ItemDto item1 = ItemTestData.createItemDto();
        ItemDto item2 = ItemTestData.createItemDto();

        String text = "text";

        when(restTemplate.exchange(
                eq("/search?text={text}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                eq(Map.of("text", text))
        )).thenReturn(ResponseEntity.ok(List.of(item1, item2)));

        ResponseEntity<Object> actualResponse = itemClient.searchItems(item1.getOwnerId(), text);

        assertNotNull(actualResponse);
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(List.of(item1, item2), actualResponse.getBody());
    }

    @Test
    void shouldUpdateItem() {
        PatchItemRequest request = ItemTestData.createPatchItemRequest();
        ItemDto updatedItem = ItemTestData.createItemDto();
        updatedItem.setId(1);
        updatedItem.setName(request.getName());
        updatedItem.setDescription(request.getDescription());
        updatedItem.setAvailable(request.getAvailable());

        when(restTemplate.exchange(
                eq("/" + updatedItem.getId()),
                eq(HttpMethod.PATCH),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(ResponseEntity.ok(updatedItem));

        ResponseEntity<Object> actualResponse =
                itemClient.updateItem(updatedItem.getOwnerId(),updatedItem.getId(), request);

        assertNotNull(actualResponse);
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(updatedItem, actualResponse.getBody());
    }

    @Test
    void shouldCreateComment() {
        ItemDto item = ItemTestData.createItemDto();
        UserDto author = UserTestData.createUserDto();
        CommentDto request = ItemTestData.createNewCommentDto();
        CommentDto savedComment = ItemTestData.createCommentDto(item, author, request);

        when(restTemplate.exchange(
                eq("/" + item.getId() + "/comment"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(new ResponseEntity<>(savedComment, HttpStatus.CREATED));

        ResponseEntity<Object> actualResponse =
                itemClient.createComment(author.getId(), item.getId(), request);

        assertNotNull(actualResponse);
        assertEquals(HttpStatus.CREATED, actualResponse.getStatusCode());
        assertEquals(savedComment, actualResponse.getBody());
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