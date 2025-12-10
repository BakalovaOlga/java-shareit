package ru.practicum.shareit.item.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsAndBookingDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingService bookingService;
    private final CommentRepository commentRepository;

    @Override
    public Item create(Item item, Long ownerId) {
        User owner = userService.getUser(ownerId);
        validateItem(item);
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    @Override
    public Item update(Item item, Long ownerId) {
        Item existingItem = getById(item.getId());

        if (!existingItem.getOwner().getId().equals(ownerId)) {
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
        validatePagination(from, size);
        Pageable pageable = PageRequest.of(from / size, size);
        return itemRepository.findByOwnerId(ownerId, pageable);
    }

    @Override
    public List<Item> search(String text, int from, int size) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        validatePagination(from, size);
        Pageable pageable = PageRequest.of(from / size, size);
        return itemRepository.findAvailableItemsByText(text, pageable);
    }

    @Override
    public void delete(Long itemId, Long ownerId) {
        Item item = getById(itemId);
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Удалять вещь может только владелец");
        }
        itemRepository.deleteById(itemId);
    }

    @Transactional
    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User user = userService.getUser(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id:" + itemId + " не найдена"));

        boolean hasFinishedBooking = bookingService.hasUserFinishedBookingForItem(userId, itemId);

        if (!hasFinishedBooking) {
            log.warn("Попытка добавления комментария " +
                    "без завершенного бронирования: userId={}, itemId={}", userId, itemId);
            throw new ValidationException("У пользователя с id " + userId +
                    " должно быть хотя бы одно завершенное бронирование предмета с id " +
                    itemId + " для возможности оставить комментарий.");
        }

        Comment comment = CommentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(user);

        comment = commentRepository.save(comment);

        log.info("Добавление комментария пользователем {} к предмету {}", userId, itemId);

        return CommentMapper.toDto(comment);
    }

    private void validatePagination(int from, int size) {
        if (from < 0) {
            throw new ValidationException("Параметр 'from' не может быть отрицательным");
        }
        if (size <= 0) {
            throw new ValidationException("Параметр 'size' должен быть положительным");
        }
    }

    public ItemWithCommentsAndBookingDto getItemWithCommentsAndBookings(Long itemId, Long userId) {
        Item item = getById(itemId);
        List<CommentDto> comments = getCommentsByItemId(itemId);

        BookingShortDto lastBooking = null;
        BookingShortDto nextBooking = null;

        if (item.getOwner().getId().equals(userId)) {
            lastBooking = bookingService.getLastBookingForItem(itemId);
            nextBooking = bookingService.getNextBookingForItem(itemId);
        }

        return ItemMapper.toItemWithCommentsAndBookingDto(item, comments, lastBooking, nextBooking);
    }

    @Override
    public List<ItemWithCommentsAndBookingDto> getItemsWithCommentsAndBookingsByOwner(Long ownerId, int from, int size) {
        List<Item> items = getAllByOwner(ownerId, from, size);

        return items.stream()
                .map(item -> {
                    List<CommentDto> comments = getCommentsByItemId(item.getId());
                    BookingShortDto lastBooking = bookingService.getLastBookingForItem(item.getId());
                    BookingShortDto nextBooking = bookingService.getNextBookingForItem(item.getId());

                    return ItemMapper.toItemWithCommentsAndBookingDto(item, comments, lastBooking, nextBooking);
                })
                .collect(Collectors.toList());
    }

    private List<CommentDto> getCommentsByItemId(Long itemId) {
        List<Comment> comments = commentRepository.findByItemId(itemId);
        return comments.stream()
                .map(CommentMapper::toDto)
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
