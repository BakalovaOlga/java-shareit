package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(
            @RequestBody @Valid ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long ownerId) {
        log.info("Запрос на создание предмета от владельца {}", ownerId);

        return itemClient.createItem(ownerId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(
            @PathVariable @Positive Long itemId,
            @RequestBody @Valid ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long ownerId) {
        log.info("Запрос на обновление предмета {} от владельца {}", itemId, ownerId);

        itemDto.setId(itemId);

        return itemClient.updateItem(ownerId, itemId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @PathVariable @Positive Long itemId,
            @RequestBody CommentDto commentDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("Запрос на добавление комментария к предмету {} от пользователя {}", itemId, userId);

        return itemClient.addComment(userId, itemId, commentDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(
            @PathVariable @Positive Long itemId,
            @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        log.info("Запрос на получение предмета {} пользователем {}", itemId, userId);

        if (userId == null) {
            return itemClient.getItemById(0, itemId);
        }

        return itemClient.getItemById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemsByOwner(
            @RequestHeader("X-Sharer-User-Id") @Positive Long ownerId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("Запрос на получение предметов владельца {} с параметрами from={}, size={}", ownerId, from, size);

        return itemClient.getItemsByOwner(ownerId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(
            @RequestParam String text,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        log.info("Поиск предметов по тексту '{}' с параметрами from={}, size={}", text, from, size);

        if (text.isBlank()) {
            return ResponseEntity.ok().body("[]");
        }

        return itemClient.searchItems(text, from, size, userId);
    }


    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(
            @PathVariable @Positive Long itemId,
            @RequestHeader("X-Sharer-User-Id") @Positive Long ownerId) {
        log.info("Запрос на удаление предмета {} от владельца {}", itemId, ownerId);

        return itemClient.deleteItem(ownerId, itemId);
    }
}