package ru.practicum.shareit.gateway.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.item.dto.CommentDto;
import ru.practicum.shareit.gateway.item.dto.ItemDetailedDto;
import ru.practicum.shareit.gateway.item.dto.ItemDto;
import ru.practicum.shareit.gateway.item.dto.PatchItemRequest;
import ru.practicum.shareit.gateway.utils.ItemTestData;
import ru.practicum.shareit.gateway.utils.RandomUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemClient itemClient;

    private final Random random = new Random();

    @Test
    void shouldCreateItem() throws Exception {
        ItemDto newItem = ItemTestData.createNewItemDto();

        ItemDto savedItem = new ItemDto();
        savedItem.setId(random.nextInt(100));
        savedItem.setName(newItem.getName());
        savedItem.setDescription(newItem.getDescription());
        savedItem.setAvailable(newItem.getAvailable());
        savedItem.setOwnerId(random.nextInt(100));

        when(itemClient.createItem(anyInt(), any(ItemDto.class)))
                .thenReturn(new ResponseEntity<>(savedItem, HttpStatus.CREATED));

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", savedItem.getOwnerId())
                        .content(objectMapper.writeValueAsString(newItem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedItem.getId()))
                .andExpect(jsonPath("$.name").value(newItem.getName()))
                .andExpect(jsonPath("$.description").value(newItem.getDescription()))
                .andExpect(jsonPath("$.available").value(newItem.getAvailable()))
                .andExpect(jsonPath("$.ownerId").value(savedItem.getOwnerId()));
    }

    @Test
    void shouldNotCreateItemOfUnknownUser() throws Exception {
        ItemDto newItem = ItemTestData.createNewItemDto();

        when(itemClient.createItem(anyInt(), any(ItemDto.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 999)
                        .content(objectMapper.writeValueAsString(newItem)))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldNotCreateItemWithNullOrEmptyName(String name) throws Exception {
        ItemDto newItem = ItemTestData.createNewItemDto();
        newItem.setName(name);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(newItem)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldNotCreateItemWithNullOrEmptyDescription(String description) throws Exception {
        ItemDto newItem = ItemTestData.createNewItemDto();
        newItem.setDescription(description);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(newItem)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateItemWithNullAvailableField() throws Exception {
        ItemDto newItem = ItemTestData.createNewItemDto();
        newItem.setAvailable(null);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(newItem)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetItemById() throws Exception {
        ItemDetailedDto item = ItemTestData.createItemDetailedDto();

        when(itemClient.getItem(anyInt(), anyInt()))
                .thenReturn(new ResponseEntity<>(item, HttpStatus.OK));

        mockMvc.perform(get("/items/" + item.getId())
                        .header("X-Sharer-User-Id", item.getOwnerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item.getId()))
                .andExpect(jsonPath("$.name").value(item.getName()))
                .andExpect(jsonPath("$.description").value(item.getDescription()))
                .andExpect(jsonPath("$.available").value(item.getAvailable()))
                .andExpect(jsonPath("$.ownerId").value(item.getOwnerId()));
    }

    @Test
    void shouldNotGetUnknownItem() throws Exception {
        when(itemClient.getItem(anyInt(), anyInt()))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/items/" + 999)
                        .header("X-Sharer-User-Id", 999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllUsersItems() throws Exception {
        ItemDetailedDto item1 = ItemTestData.createItemDetailedDto();
        ItemDetailedDto item2 = ItemTestData.createItemDetailedDto();
        item2.setOwnerId(item1.getOwnerId());

        when(itemClient.getItems(anyInt()))
                .thenReturn(new ResponseEntity<>(List.of(item1, item2), HttpStatus.OK));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", item1.getOwnerId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldNotGetItemsForUnknownUser() throws Exception {
        when(itemClient.getItems(anyInt()))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateItem() throws Exception {
        PatchItemRequest request = ItemTestData.createPatchItemRequest();

        ItemDto updatedItem = new ItemDto();
        updatedItem.setOwnerId(random.nextInt(100));
        updatedItem.setName(request.getName());
        updatedItem.setDescription(request.getDescription());
        updatedItem.setAvailable(request.getAvailable());
        updatedItem.setId(random.nextInt(100));

        when(itemClient.updateItem(anyInt(), anyInt(), any(PatchItemRequest.class)))
                .thenReturn(new ResponseEntity<>(updatedItem, HttpStatus.OK));

        mockMvc.perform(patch("/items/" + updatedItem.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", updatedItem.getOwnerId())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedItem.getId()))
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.description").value(request.getDescription()))
                .andExpect(jsonPath("$.available").value(request.getAvailable()))
                .andExpect(jsonPath("$.ownerId").value(updatedItem.getOwnerId()));
    }

    @Test
    void shouldNotUpdateIfNotFoundItemOrUser() throws Exception {
        PatchItemRequest request = ItemTestData.createPatchItemRequest();

        when(itemClient.updateItem(anyInt(), anyInt(), any(PatchItemRequest.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(patch("/items/" + 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotUpdateSomeoneElsesItem() throws Exception {
        PatchItemRequest request = ItemTestData.createPatchItemRequest();

        when(itemClient.updateItem(anyInt(), anyInt(), any(PatchItemRequest.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.FORBIDDEN));

        mockMvc.perform(patch("/items/" + 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void shouldNotUpdateItemIfNewNameIsInvalid(String name) throws Exception {
        PatchItemRequest request = ItemTestData.createPatchItemRequest();
        request.setName(name);

        mockMvc.perform(patch("/items/" + 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetItemsBySearchString() throws Exception {
        ItemDto item1 = ItemTestData.createItemDto();
        ItemDto item2 = ItemTestData.createItemDto();

        when(itemClient.searchItems(anyInt(), anyString()))
                .thenReturn(new ResponseEntity<>(List.of(item1, item2), HttpStatus.OK));

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldCreateComment() throws Exception {
        CommentDto request = ItemTestData.createNewCommentDto();

        CommentDto savedComment = new CommentDto();
        savedComment.setCreated(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()));
        savedComment.setId(1);
        savedComment.setItemId(1);
        savedComment.setAuthorName(RandomUtils.createName());
        savedComment.setText(request.getText());

        when(itemClient.createComment(anyInt(), anyInt(), any(CommentDto.class)))
                .thenReturn(new ResponseEntity<>(savedComment, HttpStatus.OK));

        mockMvc.perform(post("/items/" + 1 + "/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value(request.getText()))
                .andExpect(jsonPath("$.itemId").value(savedComment.getItemId()))
                .andExpect(jsonPath("$.authorName").value(savedComment.getAuthorName()))
                .andExpect(jsonPath("$.created").value(savedComment.getCreated()));
    }

    @Test
    void shouldNotCreateCommentIfNotFoundItemOrUser() throws Exception {
        CommentDto request = ItemTestData.createNewCommentDto();

        when(itemClient.createComment(anyInt(), anyInt(), any(CommentDto.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(post("/items/" + 1 + "/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotCreateCommentIfUserHasNoPastBookings() throws Exception {
        CommentDto request = ItemTestData.createNewCommentDto();

        when(itemClient.createComment(anyInt(), anyInt(), any(CommentDto.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mockMvc.perform(post("/items/" + 1 + "/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestIfUserHeaderIsMissing() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ItemTestData.createNewItemDto())))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/items/" + 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/items"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/items/search")
                        .param("text", "search"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/items/" + 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ItemTestData.createNewItemDto())))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/items/" + 1 + "/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ItemTestData.createNewCommentDto())))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void shouldReturnBadRequestIfUserHeaderIdNotPositive(int id) throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", id)
                        .content(objectMapper.writeValueAsString(ItemTestData.createNewItemDto())))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/items/" + 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", id))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", id))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/items/search")
                        .param("text", "search")
                        .header("X-Sharer-User-Id", id))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/items/" + 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", id)
                        .content(objectMapper.writeValueAsString(ItemTestData.createNewItemDto())))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/items/" + 1 + "/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", id)
                        .content(objectMapper.writeValueAsString(ItemTestData.createNewCommentDto())))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void shouldReturnBadRequestIfItemIdNotPositive(int id) throws Exception {
        mockMvc.perform(get("/items/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/items/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(ItemTestData.createNewItemDto())))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/items/" + id + "/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(ItemTestData.createNewCommentDto())))
                .andExpect(status().isBadRequest());
    }
}