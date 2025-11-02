package ru.practicum.shareit.gateway.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ItemRequestShortDto {
    private Integer id;
    @NotBlank(message = "Описание не может быть пустым")
    private String description;
    private Integer requestorId;
    private String created;
}
