package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto addBooking(@RequestBody BookingRequestDto request,
                                  @RequestHeader("X-Sharer-User-Id") Long userId) {
        request.setBooker(userId);
        log.info("Запрос на добавление бронирования");
        return bookingService.addBooking(request);
    }

    @PatchMapping("/{bookingsId}")
    public BookingDto respondToBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                       @PathVariable("bookingsId") Long bookingId,
                                       @RequestParam(value = "approved") Boolean approve) {
        log.info("Ответ на бронирование с id: {}", bookingId);
        return bookingService.respondToBooking(userId, bookingId, approve);

    }

    @GetMapping("/{bookingsId}")
    public BookingDto getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @PathVariable("bookingsId") Long bookingId) {
        log.info("Запрос на получение бронирования с id: {}", bookingId);
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getAllBooking(@RequestHeader("X-Sharer-User-Id") Long userIdStr,
                                          @RequestParam(value = "state", defaultValue = "ALL") String status) {
        try {
            BookingState state = BookingState.valueOf(status);
            log.info("Запрос на получение бронирований,userId: {}", userIdStr);
            return bookingService.getAllBooking(userIdStr, state);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(value = "state", defaultValue = "ALL") String state,
            @RequestParam(value = "from", defaultValue = "0") Integer from,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {

        log.info("Запрос на получение бронирований владельца {}, state: {}", userId, state);
        return bookingService.getAllItemBooking(userId, BookingState.valueOf(state));
    }
}
