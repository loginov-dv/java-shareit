package ru.practicum.shareit.server.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.shareit.server.exception.ExceptionConstants;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.request.dto.ItemRequestDto;
import ru.practicum.shareit.server.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.server.utils.ItemRequestTestData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemRequestService requestService;

    private final Random random = new Random();

    @Test
    void shouldCreateItemRequest() throws Exception {
        ItemRequestShortDto request = ItemRequestTestData.createNewItemRequest();

        ItemRequestShortDto savedRequest = new ItemRequestShortDto();
        savedRequest.setDescription(request.getDescription());
        savedRequest.setRequestorId(request.getRequestorId());
        savedRequest.setId(random.nextInt(100));
        savedRequest.setCreated(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()));

        when(requestService.createRequest(anyInt(), any(ItemRequestShortDto.class)))
                .thenReturn(savedRequest);

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", savedRequest.getRequestorId())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedRequest.getId()))
                .andExpect((jsonPath("$.description").value(savedRequest.getDescription())))
                .andExpect(jsonPath("$.requestorId").value(savedRequest.getRequestorId()))
                .andExpect(jsonPath("$.created").value(savedRequest.getCreated()));
    }

    @Test
    void shouldNotCreateItemRequestIfRequestorNotFound() throws Exception {
        when(requestService.createRequest(anyInt(), any(ItemRequestShortDto.class)))
                .thenThrow(new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, 999)));

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 999)
                        .content(objectMapper.writeValueAsString(ItemRequestTestData.createNewItemRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetItemRequestById() throws Exception {
        ItemRequestDto dto = ItemRequestTestData.createItemRequestDto();

        when(requestService.findById(anyInt()))
                .thenReturn(dto);

        mockMvc.perform(get("/requests/" + dto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect((jsonPath("$.description").value(dto.getDescription())))
                .andExpect(jsonPath("$.requestorId").value(dto.getRequestorId()))
                .andExpect(jsonPath("$.created").value(dto.getCreated()));
    }

    @Test
    void shouldNotGetUnknownItemRequest() throws Exception {
        when(requestService.findById(anyInt()))
                .thenThrow(new NotFoundException("Запрос с id = " + 999 + " не найден"));

        mockMvc.perform(get("/requests/" + 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllItemRequestsByRequestorId() throws Exception {
        ItemRequestDto itemRequest1 = ItemRequestTestData.createItemRequestDto();
        ItemRequestDto itemRequest2 = ItemRequestTestData.createItemRequestDto();
        itemRequest2.setRequestorId(itemRequest1.getRequestorId());

        when(requestService.findByUserId(anyInt()))
                .thenReturn(List.of(itemRequest1, itemRequest2));

        mockMvc.perform(get("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", itemRequest1.getRequestorId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldNotGetAllItemRequestsByRequestorIdOfUnknownRequestor() throws Exception {
        when(requestService.findByUserId(anyInt()))
                .thenThrow(new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, 999)));

        mockMvc.perform(get("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllItemRequests() throws Exception {
        ItemRequestShortDto itemRequest1 = ItemRequestTestData.createItemRequestShortDto();
        ItemRequestShortDto itemRequest2 = ItemRequestTestData.createItemRequestShortDto();

        when(requestService.findAll(anyInt()))
                .thenReturn(List.of(itemRequest1, itemRequest2));

        mockMvc.perform(get("/requests/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}