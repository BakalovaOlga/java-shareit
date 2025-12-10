package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {

    BookingDto addBooking(BookingRequestDto request);

    BookingDto getBookingById(Long userId, Long bookingId);

    List<BookingDto> getAllBooking(Long userId, BookingState state);

    BookingDto respondToBooking(Long userId, Long bookingId, Boolean status);

    List<BookingDto> getAllItemBooking(Long userId, BookingState state);

    boolean hasUserFinishedBookingForItem(Long userId, Long itemId);

    BookingShortDto getLastBookingForItem(Long itemId);

    BookingShortDto getNextBookingForItem(Long itemId);
}
