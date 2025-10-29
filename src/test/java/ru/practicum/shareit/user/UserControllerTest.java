package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.EmailConflictException;
import ru.practicum.shareit.exception.ExceptionConstants;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.PatchUserRequest;
import ru.practicum.shareit.user.dto.PostUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.utils.RandomUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;

    @Test
    void shouldCreateUser() throws Exception {
        PostUserRequest newUser = createNewUser();
        UserDto savedUser = createUserDto(newUser);

        when(userService.createUser(any()))
                .thenReturn(savedUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(newUser.getName()))
                .andExpect(jsonPath("$.email").value(newUser.getEmail()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", " ", "invalid email", "invalid_email"})
    void shouldNotCreateUserWithInvalidEmail(String email) throws Exception {
        PostUserRequest newUser = createNewUser();
        newUser.setEmail(email);

        UserDto savedUser = createUserDto(newUser);

        when(userService.createUser(any()))
                .thenReturn(savedUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldNotCreateUserWithNullOrEmptyName(String name) throws Exception {
        PostUserRequest newUser = createNewUser();
        newUser.setName(name);

        UserDto savedUser = createUserDto(newUser);

        when(userService.createUser(any()))
                .thenReturn(savedUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateUserWithAlreadyExistingEmail() throws Exception {
        when(userService.createUser(any()))
                .thenThrow(new EmailConflictException(ExceptionConstants.EMAIL_CONFLICT));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createNewUser())))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldGetUserById() throws Exception {
        UserDto savedUser = createUserDto(createNewUser());

        when(userService.findById(savedUser.getId()))
                .thenReturn(savedUser);

        mockMvc.perform(get("/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.name").value(savedUser.getName()))
                .andExpect(jsonPath("$.email").value(savedUser.getEmail()));
    }

    @Test
    void shouldNotGetUnknownUserById() throws Exception {
        when(userService.findById(999))
                .thenThrow(new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, 999)));

        mockMvc.perform(get("/users/" + 999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteUserById() throws Exception {
        doNothing().when(userService).deleteById(anyInt());

        mockMvc.perform(delete("/users/" + 999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userService, Mockito.times(1)).deleteById(999);
    }

    @Test
    void shouldUpdateUser() throws Exception {
        PatchUserRequest request = new PatchUserRequest();
        UserDto updatedUser = new UserDto();
        request.setName("name");
        request.setEmail("name@mail.ru");
        updatedUser.setId(1);
        updatedUser.setName(request.getName());
        updatedUser.setEmail(request.getEmail());

        when(userService.update(anyInt(), any()))
                .thenReturn(updatedUser);

        mockMvc.perform(patch("/users/" + 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedUser.getId()))
                .andExpect(jsonPath("$.name").value(updatedUser.getName()))
                .andExpect(jsonPath("$.email").value(updatedUser.getEmail()));
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", ""})
    void shouldNotUpdateUserIfNewNameIsInvalid(String name) throws Exception {
        PatchUserRequest request = new PatchUserRequest();
        request.setName(name);
        request.setEmail("name@mail.ru");

        mockMvc.perform(patch("/users/" + 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid email", "invalid_email"})
    void shouldNotUpdateUserIfNewEmailIsInvalid(String email) throws Exception {
        PatchUserRequest request = new PatchUserRequest();
        request.setName("name");
        request.setEmail(email);

        mockMvc.perform(patch("/users/" + 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotUpdateUnknownUser() throws Exception {
        when(userService.update(anyInt(), any()))
                .thenThrow(new NotFoundException(String.format(ExceptionConstants.USER_NOT_FOUND_BY_ID, 999)));

        PatchUserRequest request = new PatchUserRequest();
        request.setName("name");
        request.setEmail("name@mail.ru");

        mockMvc.perform(patch("/users/" + 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotUpdateUserIfNewEmailAlreadyExists() throws Exception {
        when(userService.update(anyInt(), any()))
                .thenThrow(new EmailConflictException(ExceptionConstants.EMAIL_CONFLICT));

        PatchUserRequest request = new PatchUserRequest();
        request.setName("name");
        request.setEmail("name@mail.ru");

        mockMvc.perform(patch("/users/" + 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void shouldReturnBadRequestIfUserIdNotPositive(int id) throws Exception {
        mockMvc.perform(get("/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        PatchUserRequest request = new PatchUserRequest();
        request.setName("name");
        request.setEmail("name@mail.ru");

        mockMvc.perform(patch("/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private PostUserRequest createNewUser() {
        PostUserRequest user = new PostUserRequest();
        String name = RandomUtils.createName();

        user.setName(name);
        user.setEmail(name + "@mail.ru");

        return user;
    }

    private UserDto createUserDto(PostUserRequest request) {
        UserDto dto = new UserDto();

        dto.setId(1);
        dto.setName(request.getName());
        dto.setEmail(request.getEmail());

        return dto;
    }
}