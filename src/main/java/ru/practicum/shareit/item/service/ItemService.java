package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsAndBookingDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    Item create(Item item, Long ownerId);

    Item update(Item item, Long ownerId);

    Item getById(Long itemId);

    List<Item> getAllByOwner(Long ownerId, int from, int size);

    List<Item> search(String text, int from, int size);

    void delete(Long itemId, Long ownerId);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);

    ItemWithCommentsAndBookingDto getItemWithCommentsAndBookings(Long itemId, Long userId);

    List<ItemWithCommentsAndBookingDto> getItemsWithCommentsAndBookingsByOwner(Long ownerId, int from, int size);
}
