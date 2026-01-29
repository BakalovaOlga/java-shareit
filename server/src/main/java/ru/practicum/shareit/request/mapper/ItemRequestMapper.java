package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.request.dto.ItemNewRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {
    @Mapping(target = "requestorId", source = "requestor.id")
    @Mapping(target = "requestor", ignore = true)
    @Mapping(target = "items", ignore = true)
    ItemRequestDto toDto(ItemRequest itemRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "description", source = "requestDto.description")
    @Mapping(target = "requestor", source = "requestor")
    ItemRequest toEntity(ItemNewRequestDto requestDto, User requestor);
}
