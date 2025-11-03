package ru.practicum.shareit.server.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.shareit.server.exception.ExceptionConstants;
import ru.practicum.shareit.server.exception.NoAccessException;
import ru.practicum.shareit.server.exception.NotAvailableException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.item.dto.CommentDto;
import ru.practicum.shareit.server.item.dto.ItemDetailedDto;
import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.item.dto.UpdateItemDto;
import ru.practicum.shareit.server.utils.ItemTestData;
import ru.practicum.shareit.server.utils.RandomUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

import static org.mockito.ArgumentMatchers.*;
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
    private ItemService itemService;

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

        when(itemService.createItem(anyInt(), any(ItemDto.class))).thenReturn(savedItem);

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

        when(itemService.createItem(anyInt(), any(ItemDto.class)))
                .thenThrow(new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, 999)));

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 999)
                        .content(objectMapper.writeValueAsString(newItem)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetItemById() throws Exception {
        ItemDetailedDto item = ItemTestData.createItemDetailedDto();

        when(itemService.findById(anyInt(), anyInt()))
                .thenReturn(item);

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
        when(itemService.findById(anyInt(), anyInt()))
                .thenThrow(new NotFoundException(String.format(ExceptionConstants.ITEM_NOT_FOUND_BY_ID, 999)));

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

        when(itemService.findByUserId(anyInt()))
                .thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", item1.getOwnerId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldNotGetItemsForUnknownUser() throws Exception {
        when(itemService.findByUserId(anyInt()))
                .thenThrow(new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, 999)));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateItem() throws Exception {
        UpdateItemDto request = ItemTestData.createUpdateItemDto();

        ItemDto updatedItem = new ItemDto();
        updatedItem.setOwnerId(random.nextInt(100));
        updatedItem.setName(request.getName());
        updatedItem.setDescription(request.getDescription());
        updatedItem.setAvailable(request.getAvailable());
        updatedItem.setId(random.nextInt(100));

        when(itemService.update(anyInt(), anyInt(), any(UpdateItemDto.class)))
                .thenReturn(updatedItem);

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
        UpdateItemDto request = ItemTestData.createUpdateItemDto();

        when(itemService.update(anyInt(), anyInt(), any(UpdateItemDto.class)))
                .thenThrow(new NotFoundException("not found"));

        mockMvc.perform(patch("/items/" + 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotUpdateSomeoneElsesItem() throws Exception {
        UpdateItemDto request = ItemTestData.createUpdateItemDto();

        when(itemService.update(anyInt(), anyInt(), any(UpdateItemDto.class)))
                .thenThrow(new NoAccessException("Нет доступа на изменение предмета"));

        mockMvc.perform(patch("/items/" + 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetItemsBySearchString() throws Exception {
        ItemDto item1 = ItemTestData.createItemDto();
        ItemDto item2 = ItemTestData.createItemDto();

        when(itemService.search(anyString()))
                .thenReturn(List.of(item1, item2));

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

        when(itemService.createComment(anyInt(), anyInt(), any(CommentDto.class)))
                .thenReturn(savedComment);

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

        when(itemService.createComment(anyInt(), anyInt(), any(CommentDto.class)))
                .thenThrow(new NotFoundException("not found"));

        mockMvc.perform(post("/items/" + 1 + "/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotCreateCommentIfUserHasNoPastBookings() throws Exception {
        CommentDto request = ItemTestData.createNewCommentDto();

        when(itemService.createComment(anyInt(), anyInt(), any(CommentDto.class)))
                .thenThrow(new NotAvailableException("Невозможно оставить комментарий (нет завершённой аренды)"));

        mockMvc.perform(post("/items/" + 1 + "/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}