package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public Item create(Item item, Long ownerId) {
        User owner = userService.getUser(ownerId);
        validateItem(item);
        item.setOwnerId(owner.getId());
        return itemRepository.save(item);
    }

    @Override
    public Item update(Item item, Long ownerId) {

        Item existingItem = getById(item.getId());

        if (!existingItem.getOwnerId().equals(ownerId)) {
            throw new NotFoundException("Редактировать вещь может только владелец");
        }

        if (item.getName() != null) {
            existingItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            existingItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            existingItem.setAvailable(item.getAvailable());
        }

        return itemRepository.save(existingItem);
    }

    @Override
    public Item getById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));
    }

    @Override
    public List<Item> getAllByOwner(Long ownerId, int from, int size) {
        return itemRepository.findAllByOwnerId(ownerId, from, size);
    }

    @Override
    public List<Item> search(String text, int from, int size) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text, from, size);
    }

    @Override
    public void delete(Long itemId, Long ownerId) {
        Item item = getById(itemId);
        if (!item.getOwnerId().equals(ownerId)) {
            throw new NotFoundException("Удалять вещь может только владелец");
        }
        itemRepository.deleteById(itemId);
    }

    @Override
    public List<ItemDto> findAllByRequestId(Long requestId) {
        return itemRepository.findAllByRequestId(requestId).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    private void validateItem(Item item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("Название вещи не может быть пустым");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ValidationException("Описание вещи не может быть пустым");
        }
        if (item.getAvailable() == null) {
            throw new ValidationException("Статус доступности должен быть указан");
        }
        if (item.getName().length() > 255) {
            throw new ValidationException("Название вещи слишком длинное");
        }
        if (item.getDescription().length() > 1000) {
            throw new ValidationException("Описание вещи слишком длинное");
        }
    }
}
