package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item save(Item item);

    Optional<Item> findById(Long id);

    List<Item> findAllByOwnerId(Long ownerId, int from, int size);

    List<Item> search(String text, int from, int size);

    void deleteById(Long id);

    List<Item> findAllByRequestId(Long requestId);
}
