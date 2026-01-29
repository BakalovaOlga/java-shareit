package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BookingMapperTest {

    @Autowired
    private BookingMapper bookingMapper;

    private User booker;
    private User owner;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        booker = User.builder()
                .id(1L)
                .name("Test Booker")
                .email("booker@email.com")
                .build();

        owner = User.builder()
                .id(2L)
                .name("Test Owner")
                .email("owner@email.com")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .owner(owner)
                .build();

        booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
    }

    @Test
    void toEntityWhenBookingRequestDtoIsNullShouldReturnNull() {
        Item item = Item.builder()
                .id(1L)
                .name("Test Item")
                .build();

        User user = User.builder()
                .id(1L)
                .name("Test User")
                .build();

        Booking result = bookingMapper.toEntity(null, item, user);

        assertNull(result);
    }

    @Test
    void toEntityWhenItemIsNullShouldReturnNull() {
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(1L)
                .booker(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        User user = User.builder()
                .id(1L)
                .name("Test User")
                .build();

        Booking result = bookingMapper.toEntity(requestDto, null, user);

        assertNull(result);
    }

    @Test
    void toEntityWhenBookerIsNullShouldReturnNull() {
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(1L)
                .booker(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("Test Item")
                .build();

        Booking result = bookingMapper.toEntity(requestDto, item, null);

        assertNull(result);
    }

    @Test
    void toDto() {
        BookingDto result = bookingMapper.toDto(booking);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
        assertEquals(booking.getStatus().name(), result.getStatus());
        assertNotNull(result.getItem());
        assertNotNull(result.getBooker());
        assertEquals(item.getId(), result.getItem().getId());
        assertEquals(booker.getId(), result.getBooker().getId());
    }

    @Test
    void toShortDto() {
        BookingShortDto result = bookingMapper.toShortDto(booking);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(booker.getId(), result.getBookerId());
        assertEquals(item.getId(), result.getItemId());
    }

    @Test
    void toEntity() {
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .id(10L)
                .itemId(item.getId())
                .booker(booker.getId())
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        Booking result = bookingMapper.toEntity(requestDto, item, booker);

        assertNotNull(result);
        assertEquals(requestDto.getId(), result.getId());
        assertEquals(item, result.getItem());
        assertEquals(booker, result.getBooker());
        assertEquals(requestDto.getStart(), result.getStart());
        assertEquals(requestDto.getEnd(), result.getEnd());
        assertEquals(BookingStatus.WAITING, result.getStatus());
    }
}
