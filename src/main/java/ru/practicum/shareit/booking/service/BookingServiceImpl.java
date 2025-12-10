package ru.practicum.shareit.booking.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.AccessException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Transactional
    @Override
    public BookingDto addBooking(BookingRequestDto request) {
        User booker = userRepository.findById(request.getBooker()).orElseThrow(() ->
                new NotFoundException("Пользователь с id:" + request.getBooker() + " не найден"));
        Item item = itemRepository.findById(request.getItemId()).orElseThrow(() ->
                new NotFoundException("Предмет с id:" + request.getItemId() + " не найден"));
        if (item.getOwner().getId().equals(booker.getId())) {
            throw new AccessException("Нельзя бронировать свою собственную вещь");
        }
        if (!item.getAvailable()) {
            throw new AccessException("Данная вещь не доступна для брони");
        }
        Booking booking = bookingMapper.toEntity(request, item, booker);
        booking.setStatus(BookingStatus.WAITING);
        booking = bookingRepository.save(booking);
        log.info("Добавление бронирования с id: {}", booking.getId());
        return bookingMapper.toDto(booking);
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new NotFoundException("Запрос на бронирование с id: " + bookingId + " не найден"));
        if (booking.getBooker().getId().equals(userId) || booking.getItem().getOwner().getId().equals(userId)) {
            log.info("Получение бронирования пользователя с id:{}", userId);
            return bookingMapper.toDto(booking);
        }
        throw new ValidationException("Пользователь с id: " + userId + " не является: пользователем вещи или тем кто забронировал вещь");
    }

    @Override
    public List<BookingDto> getAllBooking(Long userId, BookingState state) {
        LocalDateTime now = LocalDateTime.now();

        switch (state.toString()) {
            case "ALL":
                log.info("Получение всех бронирований пользователя с id: {}", userId);
                return bookingRepository.findByBookerIdOrderByStartDesc(userId)
                        .stream().map(bookingMapper::toDto).toList();
            case "CURRENT":
                log.info("Получение текущих бронирований пользователя с id: {}", userId);
                return bookingRepository.findCurrentByBookerId(userId, now)
                        .stream().map(bookingMapper::toDto).toList();
            case "PAST":
                log.info("Получение прошлых бронирований пользователя с id: {}", userId);
                return bookingRepository.findFinishedByBookerId(userId, now)
                        .stream().map(bookingMapper::toDto).toList();
            case "FUTURE":
                log.info("Получение будущих бронирований пользователя с id: {}", userId);
                return bookingRepository.findAllByBookerIdAndStartTimeIsAfter(userId, now)
                        .stream().map(bookingMapper::toDto).toList();
            case "WAITING":
                log.info("Получение ожидающих бронирований пользователя с id: {}", userId);
                return bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING)
                        .stream().map(bookingMapper::toDto).toList();

            case "REJECTED":
                log.info("Получение отклоненных бронирований пользователя с id: {}", userId);
                return bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED)
                        .stream().map(bookingMapper::toDto).toList();

            default:
                log.warn("Неизвестный статус бронирования: {}", state);
                throw new IllegalArgumentException("Неизвестный статус бронирования: " + state);
        }
    }

    @Transactional
    @Override
    public BookingDto respondToBooking(Long userId, Long bookingId, Boolean status) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("Пользователь с id:" + userId + " не найден"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Запрос на бронирование с id:" + bookingId + " не найден"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessException("Пользователь не является владельцем вещи. " +
                    "Давать разрешения может только владелец");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронирование уже обработано. Текущий статус: " +
                    booking.getStatus());
        }

        BookingStatus newStatus = status ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        booking.setStatus(newStatus);

        log.info("Бронирование с id: {} {} пользователем с id: {}",
                bookingId,
                status ? "подтверждено" : "отклонено",
                userId);

        return bookingMapper.toDto(booking);
    }

    @Transactional
    @Override
    public List<BookingDto> getAllItemBooking(Long userId, BookingState state) {
        LocalDateTime now = LocalDateTime.now();

        userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("Пользователь с id:" + userId + " не найден"));

        if (!itemRepository.existsByOwnerId(userId)) {
            log.warn("Пользователь с id: {} не является владельцем вещей", userId);
            throw new AccessException("Пользователь не является владельцем вещей");
        }

        switch (state.toString()) {
            case "ALL":
                log.info("Получение всех бронирований вещей пользователя с id: {}", userId);
                return bookingRepository.findByItemOwnerIdOrderByStartDesc(userId)
                        .stream().map(bookingMapper::toDto).toList();
            case "CURRENT":
                log.info("Получение текущих бронирований вещей пользователя с id: {}", userId);
                return bookingRepository.findCurrentByOwnerId(userId, now)
                        .stream().map(bookingMapper::toDto).toList();
            case "PAST":
                log.info("Получение прошлых бронирований вещей пользователя с id: {}", userId);
                return bookingRepository.findPastByOwnerId(userId, now)
                        .stream().map(bookingMapper::toDto).toList();
            case "FUTURE":
                log.info("Получение будущих бронирований вещей пользователя с id: {}", userId);
                return bookingRepository.findFutureByOwnerId(userId, now)
                        .stream().map(bookingMapper::toDto).toList();
            case "WAITING":
                log.info("Получение ожидающих бронирований вещей пользователя с id: {}", userId);
                return bookingRepository.findByOwnerIdAndStatus(userId, BookingStatus.WAITING)
                        .stream().map(bookingMapper::toDto).toList();
            case "REJECTED":
                log.info("Получение отклоненных бронирований вещей пользователя с id: {}", userId);
                return bookingRepository.findByOwnerIdAndStatus(userId, BookingStatus.REJECTED)
                        .stream().map(bookingMapper::toDto).toList();
            case "APPROVED":
                log.info("Получение подтвержденных бронирований вещей пользователя с id: {}", userId);
                return bookingRepository.findByOwnerIdAndStatus(userId, BookingStatus.APPROVED)
                        .stream().map(bookingMapper::toDto).toList();
            default:
                log.warn("Неизвестный статус бронирования вещи: {}", state);
                throw new IllegalArgumentException("Неизвестный статус бронирования: " + state);
        }
    }

    @Override
    public boolean hasUserFinishedBookingForItem(Long userId, Long itemId) {
        return bookingRepository.findFinishedByBookerId(userId, LocalDateTime.now())
                .stream()
                .anyMatch(booking -> booking.getItem().getId().equals(itemId));
    }

    @Override
    public BookingShortDto getLastBookingForItem(Long itemId) {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> lastBookings = bookingRepository.findLastBookings(itemId, now);

        if (lastBookings.isEmpty()) {
            return null;
        }

        Booking lastBooking = lastBookings.getFirst();
        return bookingMapper.toShortDto(lastBooking);
    }

    @Override
    public BookingShortDto getNextBookingForItem(Long itemId) {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> nextBookings = bookingRepository.findNextBookings(itemId, now);

        if (nextBookings.isEmpty()) {
            return null;
        }

        Booking nextBooking = nextBookings.getFirst();
        return bookingMapper.toShortDto(nextBooking);
    }
}
