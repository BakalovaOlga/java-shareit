package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private long idCounter = 1;

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(idCounter++);
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findAllByOwnerId(Long ownerId, int from, int size) {
        return items.values().stream()
                .filter(item -> item.getOwnerId().equals(ownerId))
                .sorted(Comparator.comparing(Item::getId))
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> search(String text, int from, int size) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String searchText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> item.getAvailable() &&
                        (item.getName().toLowerCase().contains(searchText) ||
                                item.getDescription().toLowerCase().contains(searchText)))
                .sorted(Comparator.comparing(Item::getId))
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        items.remove(id);
    }

    @Override
    public List<Item> findAllByRequestId(Long requestId) {
        return items.values().stream()
                .filter(item -> requestId.equals(item.getRequestId()))
                .collect(Collectors.toList());
    }
}
