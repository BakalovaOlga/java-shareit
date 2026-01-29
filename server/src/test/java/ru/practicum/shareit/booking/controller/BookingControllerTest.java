package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private Long userId;
    private Long bookingId;
    private Long itemId;
    private BookingRequestDto bookingRequestDto;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        userId = 1L;
        bookingId = 1L;

        bookingRequestDto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        bookingDto = new BookingDto(
                bookingId,
                new ItemDto(itemId, "Дрель", "Мощная дрель", true, userId, null),
                new UserDto(userId, "Иван Иванов", "ivan@mail.ru"),
                bookingRequestDto.getStart(),
                bookingRequestDto.getEnd(),
                "WAITING"
        );
    }

    // POST /bookings
    @Test
    void addBooking() throws Exception {
        when(bookingService.addBooking(any(BookingRequestDto.class)))
                .thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.item.id").value(itemId))
                .andExpect(jsonPath("$.booker.id").value(userId))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    // PATCH /bookings/{bookingId}
    @Test
    void respondToBooking() throws Exception {
        BookingDto approvedBooking = new BookingDto(
                bookingId,
                new ItemDto(itemId, "Дрель", "Описание", true, userId, null),
                new UserDto(userId, "Иван", "ivan@mail.test"),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                "APPROVED"
        );

        when(bookingService.respondToBooking(userId, bookingId, true))
                .thenReturn(approvedBooking);

        mockMvc.perform(patch("/bookings/{bookingsId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    // GET /bookings/{bookingsId}
    @Test
    void getBookingById() throws Exception {
        when(bookingService.getBookingById(userId, bookingId))
                .thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/{bookingsId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.item.id").value(itemId))
                .andExpect(jsonPath("$.booker.id").value(userId));
    }

    // GET /bookings
    @Test
    void getAllBooking() throws Exception {
        List<BookingDto> bookings = List.of(
                new BookingDto(1L,
                        new ItemDto(1L, "Дрель", "Описание", true, userId, null),
                        new UserDto(1L, "Иван", "ivan@mail.ru"),
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2),
                        "WAITING"),
                new BookingDto(2L,
                        new ItemDto(2L, "Молоток", "Описание", true, userId, null),
                        new UserDto(2L, "Петр", "petr@mail.ru"),
                        LocalDateTime.now().plusDays(3),
                        LocalDateTime.now().plusDays(4),
                        "APPROVED")
        );

        when(bookingService.getAllBooking(userId, BookingState.ALL))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    public void getAllBooking_whenInvalidState_shouldThrowException() throws Exception {
        Long userId = 1L;
        String invalidState = "INVALID_STATE";

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", invalidState))
                .andExpect(status().is5xxServerError());
    }
}