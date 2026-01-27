package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsAndBookingDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemService itemService;
    private final ItemMapper itemMapper;
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemDto createItem(
            @RequestBody @Valid ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long ownerId) {
        Item item = itemMapper.toEntity(itemDto);

        if (itemDto.getRequestId() != null) {
            ItemRequest request = itemRequestService.getRequestEntityById(itemDto.getRequestId());
            item.setRequest(request);
        }

        Item createdItem = itemService.create(item, ownerId);
        return itemMapper.toDto(createdItem);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(
            @PathVariable @Positive Long itemId,
            @RequestBody ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long ownerId) {
        itemDto.setId(itemId);
        Item item = itemMapper.toEntity(itemDto);
        Item updatedItem = itemService.update(item, ownerId);
        return itemMapper.toDto(updatedItem);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(
            @PathVariable @Positive Long itemId,
            @RequestBody CommentDto commentDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemService.addComment(userId, itemId, commentDto);
    }

    @GetMapping("/{itemId}")
    public ItemWithCommentsAndBookingDto getItemById(
            @PathVariable @Positive Long itemId,
            @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        return itemService.getItemWithCommentsAndBookings(itemId, userId);
    }

    @GetMapping
    public List<ItemWithCommentsAndBookingDto> getAllItemsByOwner(
            @RequestHeader("X-Sharer-User-Id") @Positive Long ownerId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        return itemService.getItemsWithCommentsAndBookingsByOwner(ownerId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(
            @RequestParam String text,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        List<Item> items = itemService.search(text, from, size);
        return items.stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(
            @PathVariable @Positive Long itemId,
            @RequestHeader("X-Sharer-User-Id") @Positive Long ownerId) {
        itemService.delete(itemId, ownerId);
    }
}