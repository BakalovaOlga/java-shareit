package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemNewRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemService itemService;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public ItemRequestDto createRequest(Long userId, ItemNewRequestDto requestDto) {
        validateRequestDto(requestDto);
        User user = userService.getUser(userId);

        ItemRequest request = itemRequestMapper.toEntity(requestDto, user);
        request.setCreated(LocalDateTime.now());

        ItemRequest saved = itemRequestRepository.save(request);
        return itemRequestMapper.toDto(saved);
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        validateUserExists(userId);
        ItemRequest request = getRequestEntityById(requestId);
        ;

        ItemRequestDto dto = toDtoWithItems(request);
        log.info("Получен запрос с ID: {} для пользователя с ID: {}", requestId, userId);

        return dto;
    }

    @Override
    public ItemRequest getRequestEntityById(Long requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с ID " + requestId + " не найден"));
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId, Pageable pageable) {
        validateUserExists(userId);

        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdSorted(userId, pageable);

        List<ItemRequestDto> dtos = requests.stream()
                .map(itemRequestMapper::toDto)
                .collect(Collectors.toList());

        addItemsToRequests(dtos);
        log.info("Получено {} запросов пользователя с ID: {}", dtos.size(), userId);

        return dtos;
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, Pageable pageable) {
        validateUserExists(userId);

        List<ItemRequest> requests = itemRequestRepository.findAllByNotRequestorIdSorted(userId, pageable);

        List<ItemRequestDto> dtos = requests.stream()
                .map(itemRequestMapper::toDto)
                .collect(Collectors.toList());

        addItemsToRequests(dtos);
        log.info("Получено {} запросов других пользователей для пользователя с ID: {}", dtos.size(), userId);

        return dtos;
    }

    private void validateRequestDto(ItemNewRequestDto requestDto) {
        if (requestDto == null) {
            throw new ValidationException("Запрос не может быть null");
        }
        if (requestDto.getDescription() == null || requestDto.getDescription().isBlank()) {
            throw new ValidationException("Описание запроса не может быть пустым");
        }
    }

    private void validateUserExists(Long userId) {
        userService.getUser(userId);
    }

    private ItemRequestDto toDtoWithItems(ItemRequest request) {
        ItemRequestDto dto = itemRequestMapper.toDto(request);
        List<ItemDto> items = itemService.getItemsByRequestId(request.getId());
        dto.setItems(items);
        return dto;
    }

    private void addItemsToRequests(List<ItemRequestDto> requests) {
        if (requests.isEmpty()) {
            return;
        }
        List<Long> requestIds = requests.stream()
                .map(ItemRequestDto::getId)
                .collect(Collectors.toList());

        Map<Long, List<ItemDto>> itemsByRequest = itemService.getItemsByRequestIds(requestIds);

        requests.forEach(requestDto -> {
            List<ItemDto> items = itemsByRequest.getOrDefault(requestDto.getId(), List.of());
            requestDto.setItems(items);
        });
    }
}
