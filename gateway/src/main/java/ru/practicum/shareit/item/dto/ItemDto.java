package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;
    @NotBlank(message = "Название должно быть указано")
    @Size(max = 255, message = "Название вещи слишком длинное")
    private String name;

    @NotBlank(message = "Описание не может быть пустым")
    @Size(max = 1000, message = "Описание вещи слишком длинное")
    private String description;

    @NotNull(message = "Статус доступности должен быть указан")
    private Boolean available;

    private Long ownerId;
    private Long requestId;
}
