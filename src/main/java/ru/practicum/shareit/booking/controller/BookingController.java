package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto addBooking(@Valid @RequestBody BookingRequestDto request,
                                 @Positive @RequestHeader("X-Sharer-User-Id") Long userId) {
        request.setBooker(userId);
        log.info("Запрос на добавление бронирования");
        return bookingService.addBooking(request);
    }

    @PatchMapping("/{bookingsId}")
    public BookingDto respondToBooking(@Positive @RequestHeader("X-Sharer-User-Id") Long userId,
                                       @Positive @PathVariable("bookingsId") Long bookingId,
                                       @RequestParam(value = "approved") Boolean approve) {
        log.info("Ответ на бронирование с id: {}", bookingId);
        return bookingService.respondToBooking(userId, bookingId, approve);

    }

    @GetMapping("/{bookingsId}")
    public BookingDto getBookingById(@Positive @RequestHeader("X-Sharer-User-Id") Long userId,
                                     @Positive @PathVariable("bookingsId") Long bookingId) {
        log.info("Запрос на получение бронирования с id: {}", bookingId);
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getAllBooking(@Positive @RequestHeader("X-Sharer-User-Id") Long userIdStr,
                                          @RequestParam(value = "state", defaultValue = "ALL") String status) {
        try {
            BookingState state = BookingState.valueOf(status);
            log.info("Запрос на получение бронирований,userId: {}", userIdStr);
            return bookingService.getAllBooking(userIdStr, state);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }
}
