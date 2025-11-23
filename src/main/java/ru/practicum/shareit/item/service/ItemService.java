package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    Item create(Item item, Long ownerId);

    Item update(Item item, Long ownerId);

    Item getById(Long itemId);

    List<Item> getAllByOwner(Long ownerId, int from, int size);

    List<Item> search(String text, int from, int size);

    void delete(Long itemId, Long ownerId);

    List<ItemDto> findAllByRequestId(Long requestId);
}
