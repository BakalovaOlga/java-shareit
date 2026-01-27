package ru.practicum.shareit.request.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.ItemNewRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createRequest(Long userId, ItemNewRequestDto requestDto);

    ItemRequestDto getRequestById(Long userId, Long requestId);

    ItemRequest getRequestEntityById(Long requestId);

    List<ItemRequestDto> getUserRequests(Long userId, Pageable pageable);

    List<ItemRequestDto> getAllRequests(Long userId, Pageable pageable);
}
