package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto createItem(
            @RequestBody @Valid ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        Item item = ItemMapper.toEntity(itemDto);
        Item createdItem = itemService.create(item, ownerId);
        return ItemMapper.toDto(createdItem);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(
            @PathVariable Long itemId,
            @RequestBody ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        itemDto.setId(itemId);
        Item item = ItemMapper.toEntity(itemDto);
        Item updatedItem = itemService.update(item, ownerId);
        return ItemMapper.toDto(updatedItem);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(
            @PathVariable Long itemId,
            @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        Item item = itemService.getById(itemId);
        return ItemMapper.toDto(item);
    }

    @GetMapping
    public List<ItemDto> getAllItemsByOwner(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        List<Item> items = itemService.getAllByOwner(ownerId, from, size);
        return items.stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(
            @RequestParam String text,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        List<Item> items = itemService.search(text, from, size);
        return items.stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        itemService.delete(itemId, ownerId);
    }
}
