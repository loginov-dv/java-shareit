package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.dto.PatchItemRequest;
import ru.practicum.shareit.user.dto.PostUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateItem() throws Exception {
        ItemShortDto item = createItem();
        int ownerId = createUserAndGetId();

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(item.getName()))
                .andExpect(jsonPath("$.description").value(item.getDescription()))
                .andExpect(jsonPath("$.available").value(item.getAvailable()))
                .andExpect(jsonPath("$.ownerId").value(ownerId));
    }

    @Test
    void shouldNotCreateItemWithoutOwnerId() throws Exception {
        ItemShortDto item = createItem();

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateItemForUnknownUser() throws Exception {
        ItemShortDto item = createItem();

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 9999)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotCreateItemWithoutName() throws Exception {
        ItemShortDto item = createItem();
        item.setName(null);
        int ownerId = createUserAndGetId();

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest());

        item.setName("");

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateItemWithoutDescription() throws Exception {
        ItemShortDto item = createItem();
        item.setDescription(null);
        int ownerId = createUserAndGetId();

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest());

        item.setDescription("");

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateItemWithoutAvailableField() throws Exception {
        ItemShortDto item = createItem();
        item.setAvailable(null);
        int ownerId = createUserAndGetId();

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetItem() throws Exception {
        ItemShortDto item = createItem();
        int ownerId = createUserAndGetId();

        MvcResult response = mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andReturn();

        String json = response.getResponse().getContentAsString();
        ItemShortDto itemDto = objectMapper.readValue(json, ItemShortDto.class);

        mockMvc.perform(get("/items/" + itemDto.getId())
                        .header("X-Sharer-User-Id", ownerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(item.getName()))
                .andExpect(jsonPath("$.description").value(item.getDescription()))
                .andExpect(jsonPath("$.available").value(item.getAvailable()))
                .andExpect(jsonPath("$.ownerId").value(ownerId));
    }

    @Test
    void shouldGetAllUsersItems() throws Exception {
        ItemShortDto item1 = createItem();
        ItemShortDto item2 = createItem();
        int ownerId = createUserAndGetId();

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(item1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(item2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/items")
                .header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldUpdateItem() throws Exception {
        ItemShortDto item = createItem();
        int ownerId = createUserAndGetId();

        MvcResult response = mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andReturn();

        String json = response.getResponse().getContentAsString();
        ItemShortDto itemDto = objectMapper.readValue(json, ItemShortDto.class);

        PatchItemRequest updatedItem = new PatchItemRequest();
        updatedItem.setName("new name");
        updatedItem.setDescription("new description");
        updatedItem.setAvailable(false);

        mockMvc.perform(patch("/items/" + itemDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/items/" + itemDto.getId())
                        .header("X-Sharer-User-Id", ownerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(updatedItem.getName()))
                .andExpect(jsonPath("$.description").value(updatedItem.getDescription()))
                .andExpect(jsonPath("$.available").value(updatedItem.getAvailable()));
    }

    @Test
    void shouldNotUpdateItemWithoutOwnerId() throws Exception {
        ItemShortDto item = createItem();
        int ownerId = createUserAndGetId();

        MvcResult response = mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andReturn();

        String json = response.getResponse().getContentAsString();
        ItemShortDto itemDto = objectMapper.readValue(json, ItemShortDto.class);

        PatchItemRequest updatedItem = new PatchItemRequest();
        updatedItem.setName("new name");
        updatedItem.setDescription("new description");
        updatedItem.setAvailable(false);

        mockMvc.perform(patch("/items/" + itemDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotUpdateSomeoneElsesItem() throws Exception {
        ItemShortDto item = createItem();
        int ownerId = createUserAndGetId();

        MvcResult response = mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andReturn();

        String json = response.getResponse().getContentAsString();
        ItemShortDto itemDto = objectMapper.readValue(json, ItemShortDto.class);

        PatchItemRequest updatedItem = new PatchItemRequest();
        updatedItem.setName("new name");
        updatedItem.setDescription("new description");
        updatedItem.setAvailable(false);

        int someoneElse = createUserAndGetId();

        mockMvc.perform(patch("/items/" + itemDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", someoneElse)
                        .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotUpdateWithInvalidName() throws Exception {
        ItemShortDto item = createItem();
        int ownerId = createUserAndGetId();

        MvcResult response = mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andReturn();

        String json = response.getResponse().getContentAsString();
        ItemShortDto itemDto = objectMapper.readValue(json, ItemShortDto.class);

        PatchItemRequest updatedItem = new PatchItemRequest();
        updatedItem.setName("");
        updatedItem.setDescription("new description");
        updatedItem.setAvailable(false);

        mockMvc.perform(patch("/items/" + itemDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetItemsBySearchString() throws Exception {
        ItemShortDto item1 = createItem();
        item1.setName("regerger search regerge");
        ItemShortDto item2 = createItem();
        item2.setDescription("search rwegergeg");
        int ownerId = createUserAndGetId();

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(item1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(item2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", ownerId)
                        .param("text", "search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldNotGetItemsIfSearchStringIsEmpty() throws Exception {
        int userId = createUserAndGetId();

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldNotFindUnavailableItems() throws Exception {
        ItemShortDto item = createItem();
        item.setName("unavailable");
        item.setAvailable(false);
        int ownerId = createUserAndGetId();

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", ownerId)
                        .param("text", "unavailable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private int createUserAndGetId() throws Exception {
        PostUserRequest user = createUser();

        MvcResult response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andReturn();

        String json = response.getResponse().getContentAsString();
        UserDto userDto = objectMapper.readValue(json, UserDto.class);

        return userDto.getId();
    }

    private PostUserRequest createUser() {
        PostUserRequest user = new PostUserRequest();
        String name = createName();

        user.setName(name);
        user.setEmail(name + "@mail.ru");

        return user;
    }

    private ItemShortDto createItem() {
        ItemShortDto item = new ItemShortDto();

        item.setName(createName());
        item.setDescription(createName(50));
        item.setAvailable(true);

        return item;
    }

    private String createName() {
        return createName(10);
    }

    private String createName(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        int charsLength = chars.length();
        int counter = 0;
        String result = "";

        while (counter < length) {
            result += chars.charAt((int)Math.round(Math.random() * (charsLength - 1)));
            counter++;
        }

        return result;
    }
}