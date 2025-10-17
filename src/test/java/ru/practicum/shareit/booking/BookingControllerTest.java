package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.PostBookingRequest;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.dto.PatchItemRequest;
import ru.practicum.shareit.user.dto.PostUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.utils.RandomUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {"/schema.sql", "/clear.sql"})
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateBooking() throws Exception {
        ItemShortDto item = createItem();
        UserDto booker = createUser();
        PostBookingRequest request = createBookingRequest();

        request.setItemId(item.getId());
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value(BookingStatus.WAITING.name()))
                .andExpect(jsonPath("$.start").value(request.getStart()))
                .andExpect(jsonPath("$.end").value(request.getEnd()))
                .andExpect(jsonPath("$.booker.id").value(booker.getId()))
                .andExpect(jsonPath("$.item.id").value(item.getId()));
    }

    @Test
    void shouldNotCreateBookingForUnavailableItem() throws Exception {
        ItemShortDto item = createItem();
        PatchItemRequest updatedItem = new PatchItemRequest();

        updatedItem.setAvailable(false);
        mockMvc.perform(patch("/items/" + item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", item.getOwnerId())
                        .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isOk());

        UserDto booker = createUser();
        PostBookingRequest request = createBookingRequest();

        request.setItemId(item.getId());
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateBookingForUnknownUser() throws Exception {
        ItemShortDto item = createItem();
        PostBookingRequest request = createBookingRequest();

        request.setItemId(item.getId());
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 99999)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotCreateBookingForUnknownItem() throws Exception {
        UserDto booker = createUser();
        PostBookingRequest request = createBookingRequest();

        request.setItemId(99999);
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotCreateBookingIfDatesAreWrong() throws Exception {
        ItemShortDto item = createItem();
        UserDto booker = createUser();
        PostBookingRequest request = createBookingRequest();

        request.setItemId(item.getId());

        // start = null
        request.setStart(null);
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // start = end
        request.setStart(request.getEnd());
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // end = null
        request.setEnd(null);
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // end in the past
        request.setEnd(LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // start in the past
        request.setEnd(LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        request.setStart(LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldApproveBooking() throws Exception {
        ItemShortDto item = createItem();
        UserDto booker = createUser();
        PostBookingRequest request = createBookingRequest();

        request.setItemId(item.getId());

        MvcResult response = mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        String json = response.getResponse().getContentAsString();
        BookingDto bookingDto = objectMapper.readValue(json, BookingDto.class);

        mockMvc.perform(patch("/bookings/" + bookingDto.getId())
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", item.getOwnerId()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotApproveBookingByWrongUser() throws Exception {
        ItemShortDto item = createItem();
        UserDto booker = createUser();
        PostBookingRequest request = createBookingRequest();

        request.setItemId(item.getId());

        MvcResult response = mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        String json = response.getResponse().getContentAsString();
        BookingDto bookingDto = objectMapper.readValue(json, BookingDto.class);

        mockMvc.perform(patch("/bookings/" + bookingDto.getId())
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", booker.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetBookingByBooker() throws Exception {
        ItemShortDto item = createItem();
        UserDto booker = createUser();
        PostBookingRequest request = createBookingRequest();

        request.setItemId(item.getId());

        MvcResult response = mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        String json = response.getResponse().getContentAsString();
        BookingDto bookingDto = objectMapper.readValue(json, BookingDto.class);

        mockMvc.perform(get("/bookings/" + bookingDto.getId())
                        .header("X-Sharer-User-Id", booker.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value(BookingStatus.WAITING.name()))
                .andExpect(jsonPath("$.booker.id").value(booker.getId()))
                .andExpect(jsonPath("$.item.id").value(item.getId()));
    }

    @Test
    void shouldGetBookingByOwner() throws Exception {
        ItemShortDto item = createItem();
        UserDto booker = createUser();
        PostBookingRequest request = createBookingRequest();

        request.setItemId(item.getId());

        MvcResult response = mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        String json = response.getResponse().getContentAsString();
        BookingDto bookingDto = objectMapper.readValue(json, BookingDto.class);

        mockMvc.perform(get("/bookings/" + bookingDto.getId())
                        .header("X-Sharer-User-Id", item.getOwnerId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value(BookingStatus.WAITING.name()))
                .andExpect(jsonPath("$.booker.id").value(booker.getId()))
                .andExpect(jsonPath("$.item.id").value(item.getId()));
    }

    @Test
    void shouldNotGetBookingByWrongUser() throws Exception {
        ItemShortDto item = createItem();
        UserDto booker = createUser();
        PostBookingRequest request = createBookingRequest();

        request.setItemId(item.getId());

        MvcResult response = mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        String json = response.getResponse().getContentAsString();
        BookingDto bookingDto = objectMapper.readValue(json, BookingDto.class);
        UserDto alien = createUser();

        mockMvc.perform(get("/bookings/" + bookingDto.getId())
                        .header("X-Sharer-User-Id", alien.getId()))
                .andExpect(status().isForbidden());
    }

    private PostBookingRequest createBookingRequest() {
        PostBookingRequest request = new PostBookingRequest();

        request.setStart(LocalDateTime.now().plusMinutes(10).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        request.setEnd(LocalDateTime.now().plusMinutes(15).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

        return request;
    }

    private ItemShortDto createItem() throws Exception {
        ItemShortDto request = new ItemShortDto();

        request.setName(RandomUtils.createName());
        request.setDescription(RandomUtils.createName(50));
        request.setAvailable(true);

        UserDto user = createUser();

        MvcResult response = mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", user.getId())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String json = response.getResponse().getContentAsString();
        return objectMapper.readValue(json, ItemShortDto.class);
    }

    private UserDto createUser() throws Exception {
        PostUserRequest request = new PostUserRequest();
        String name = RandomUtils.createName();

        request.setName(name);
        request.setEmail(name + "@mail.ru");

        MvcResult response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String json = response.getResponse().getContentAsString();

        return objectMapper.readValue(json, UserDto.class);
    }
}