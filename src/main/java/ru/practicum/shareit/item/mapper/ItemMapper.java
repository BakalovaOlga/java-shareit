package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

@UtilityClass
public class ItemMapper {

    public static ItemDto toDto(Item item) {
        if (item == null) return null;
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwnerId(),
                item.getRequestId()
        );
    }

    public static Item toEntity(ItemDto dto) {
        if (dto == null) return null;
        Item item = new Item();
        item.setId(dto.getId());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable());
        item.setOwnerId(dto.getOwnerId());
        item.setRequestId(dto.getRequestId());
        return item;
    }
}