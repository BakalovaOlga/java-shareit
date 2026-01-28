package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> addBooking(@Valid @RequestBody BookingRequestDto request,
                                             @Positive @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос на добавление бронирования от пользователя {}", userId);

        if (request.getEnd().isBefore(request.getStart()) || request.getEnd().isEqual(request.getStart())) {
            throw new IllegalArgumentException("Дата окончания бронирования должна быть позже даты начала");
        }

        return bookingClient.bookItem(userId, request);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> respondToBooking(@Positive @RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @Positive @PathVariable("bookingId") Long bookingId,
                                                   @RequestParam(value = "approved") Boolean approved) {
        log.info("Ответ на бронирование с id: {} от пользователя {}", bookingId, userId);
        return bookingClient.respondToBooking(userId, bookingId, approved);
    }


    @GetMapping("/{bookingsId}")
    public ResponseEntity<Object> getBookingById(@Positive @RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @Positive @PathVariable("bookingsId") Long bookingId) {
        log.info("Запрос на получение бронирования с id: {}", bookingId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllBooking(@Positive @RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestParam(value = "state", defaultValue = "ALL") String state,
                                                @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") Integer from,
                                                @Positive @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.info("Запрос на получение бронирований пользователя {}, state: {}", userId, state);
        return bookingClient.getBookings(userId, BookingState.valueOf(state), from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(
            @Positive @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(value = "state", defaultValue = "ALL") String state,
            @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(value = "size", defaultValue = "10") Integer size) {

        log.info("Запрос на получение бронирований ВЛАДЕЛЬЦА {}, state: {}", userId, state);
        return bookingClient.getOwnerBookings(userId, BookingState.valueOf(state), from, size);
    }
}
