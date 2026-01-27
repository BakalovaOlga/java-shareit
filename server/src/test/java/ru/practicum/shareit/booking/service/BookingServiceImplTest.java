package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceImplTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private User anotherUser;
    private Item availableItem;
    private Item unavailableItem;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        owner = User.builder()
                .name("Owner")
                .email("owner@email.com")
                .build();
        owner = userRepository.save(owner);

        booker = User.builder()
                .name("Booker")
                .email("booker@email.com")
                .build();
        booker = userRepository.save(booker);

        anotherUser = User.builder()
                .name("Another User")
                .email("another@email.com")
                .build();
        anotherUser = userRepository.save(anotherUser);

        availableItem = Item.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .owner(owner)
                .build();
        availableItem = itemRepository.save(availableItem);

        unavailableItem = Item.builder()
                .name("Молоток")
                .description("Строительный молоток")
                .available(false)
                .owner(owner)
                .build();
        unavailableItem = itemRepository.save(unavailableItem);
    }

    private BookingRequestDto createBookingRequestDto(Long itemId, LocalDateTime start, LocalDateTime end) {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setBooker(booker.getId());
        dto.setItemId(itemId);
        dto.setStart(start);
        dto.setEnd(end);
        return dto;
    }

    @Test
    void addBookingShouldCreateBookingSuccessfully() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingRequestDto request = createBookingRequestDto(availableItem.getId(), start, end);

        BookingDto result = bookingService.addBooking(request);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(availableItem.getId(), result.getItem().getId());
        assertEquals(booker.getId(), result.getBooker().getId());
        assertEquals("WAITING", result.getStatus());
    }

    @Test
    void addBookingShouldThrowNotFoundExceptionWhenBookerNotFound() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingRequestDto request = createBookingRequestDto(availableItem.getId(), start, end);
        request.setBooker(999L);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(request));
        assertTrue(exception.getMessage().contains("Пользователь с id:999"));
    }

    @Test
    void addBookingShouldThrowNotFoundExceptionWhenItemNotFound() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingRequestDto request = createBookingRequestDto(999L, start, end);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(request));
        assertTrue(exception.getMessage().contains("Предмет с id:999"));
    }

    @Test
    void addBookingShouldThrowAccessExceptionWhenOwnerTriesToBookOwnItem() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingRequestDto request = createBookingRequestDto(availableItem.getId(), start, end);
        request.setBooker(owner.getId());

        AccessException exception = assertThrows(AccessException.class,
                () -> bookingService.addBooking(request));
        assertEquals("Нельзя бронировать свою собственную вещь", exception.getMessage());
    }

    @Test
    void addBookingShouldThrowAccessExceptionWhenItemUnavailable() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingRequestDto request = createBookingRequestDto(unavailableItem.getId(), start, end);

        AccessException exception = assertThrows(AccessException.class,
                () -> bookingService.addBooking(request));
        assertEquals("Данная вещь не доступна для брони", exception.getMessage());
    }

    @Test
    void getBookingByIdShouldReturnBookingForBooker() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingRequestDto request = createBookingRequestDto(availableItem.getId(), start, end);
        BookingDto created = bookingService.addBooking(request);

        BookingDto result = bookingService.getBookingById(booker.getId(), created.getId());

        assertNotNull(result);
        assertEquals(created.getId(), result.getId());
        assertEquals(availableItem.getId(), result.getItem().getId());
    }

    @Test
    void getBookingByIdShouldReturnBookingForOwner() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingRequestDto request = createBookingRequestDto(availableItem.getId(), start, end);
        BookingDto created = bookingService.addBooking(request);

        BookingDto result = bookingService.getBookingById(owner.getId(), created.getId());

        assertNotNull(result);
        assertEquals(created.getId(), result.getId());
    }

    @Test
    void getBookingByIdShouldThrowNotFoundExceptionWhenBookingNotFound() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(booker.getId(), 999L));
        assertTrue(exception.getMessage().contains("Запрос на бронирование с id: 999"));
    }

    @Test
    void getBookingByIdShouldThrowValidationExceptionWhenUserIsNotBookerOrOwner() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingRequestDto request = createBookingRequestDto(availableItem.getId(), start, end);
        BookingDto created = bookingService.addBooking(request);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.getBookingById(anotherUser.getId(), created.getId()));
        assertTrue(exception.getMessage().contains("не является: пользователем вещи или тем кто забронировал вещь"));
    }

    @Test
    void respondToBookingShouldApproveBookingSuccessfully() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingRequestDto request = createBookingRequestDto(availableItem.getId(), start, end);
        BookingDto created = bookingService.addBooking(request);

        BookingDto result = bookingService.respondToBooking(owner.getId(), created.getId(), true);

        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    void respondToBookingShouldRejectBookingSuccessfully() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingRequestDto request = createBookingRequestDto(availableItem.getId(), start, end);
        BookingDto created = bookingService.addBooking(request);

        BookingDto result = bookingService.respondToBooking(owner.getId(), created.getId(), false);

        assertNotNull(result);
        assertEquals("REJECTED", result.getStatus());
    }

    @Test
    void respondToBookingShouldThrowValidationExceptionWhenUserNotFound() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingRequestDto request = createBookingRequestDto(availableItem.getId(), start, end);
        BookingDto created = bookingService.addBooking(request);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.respondToBooking(999L, created.getId(), true));
        assertTrue(exception.getMessage().contains("Пользователь с id:999"));
    }

    @Test
    void respondToBookingShouldThrowNotFoundExceptionWhenBookingNotFound() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.respondToBooking(owner.getId(), 999L, true));
        assertTrue(exception.getMessage().contains("Запрос на бронирование с id:999"));
    }

    @Test
    void respondToBookingShouldThrowAccessExceptionWhenUserIsNotOwner() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingRequestDto request = createBookingRequestDto(availableItem.getId(), start, end);
        BookingDto created = bookingService.addBooking(request);

        AccessException exception = assertThrows(AccessException.class,
                () -> bookingService.respondToBooking(anotherUser.getId(), created.getId(), true));
        assertTrue(exception.getMessage().contains("Пользователь не является владельцем вещи"));
    }

    @Test
    void respondToBookingShouldThrowValidationExceptionWhenBookingAlreadyProcessed() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        Booking booking = Booking.builder()
                .start(start)
                .end(end)
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        booking = bookingRepository.save(booking);

        Booking testBooking = booking;

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.respondToBooking(owner.getId(), testBooking.getId(), true));
        assertTrue(exception.getMessage().contains("Бронирование уже обработано"));
    }

    @Test
    void getAllBookingShouldReturnAllBookingsForUser() {
        LocalDateTime start1 = LocalDateTime.now().plusHours(1);
        LocalDateTime end1 = LocalDateTime.now().plusDays(1);
        BookingRequestDto request1 = createBookingRequestDto(availableItem.getId(), start1, end1);
        bookingService.addBooking(request1);

        List<BookingDto> result = bookingService.getAllBooking(booker.getId(), BookingState.ALL);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllBookingShouldReturnCurrentBookingsWithCurrentState() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        Booking booking = Booking.builder()
                .start(start)
                .end(end)
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(booking);

        List<BookingDto> result = bookingService.getAllBooking(booker.getId(), BookingState.CURRENT);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getAllBookingShouldReturnPastBookingsWithPastState() {
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now().minusDays(1);

        Booking booking = Booking.builder()
                .start(start)
                .end(end)
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(booking);

        List<BookingDto> result = bookingService.getAllBooking(booker.getId(), BookingState.PAST);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getAllBookingShouldReturnFutureBookingsWithFutureState() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        Booking booking = Booking.builder()
                .start(start)
                .end(end)
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        bookingRepository.save(booking);

        List<BookingDto> result = bookingService.getAllBooking(booker.getId(), BookingState.FUTURE);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllBookingShouldReturnWaitingBookingsWithWaitingState() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingRequestDto request = createBookingRequestDto(availableItem.getId(), start, end);
        bookingService.addBooking(request);

        List<BookingDto> result = bookingService.getAllBooking(booker.getId(), BookingState.WAITING);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("WAITING", result.getFirst().getStatus());
    }

    @Test
    void getAllBookingShouldReturnRejectedBookingsWithRejectedState() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        Booking booking = Booking.builder()
                .start(start)
                .end(end)
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.REJECTED)
                .build();
        bookingRepository.save(booking);

        List<BookingDto> result = bookingService.getAllBooking(booker.getId(), BookingState.REJECTED);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("REJECTED", result.getFirst().getStatus());
    }

    @Test
    void getAllBookingShouldHandleAllBookingStateValues() {
        for (BookingState state : BookingState.values()) {
            try {
                List<BookingDto> result = bookingService.getAllBooking(booker.getId(), state);
                assertNotNull(result);
            } catch (IllegalArgumentException e) {
                fail("BookingState value " + state + " is not handled properly: " + e.getMessage());
            }
        }
    }

    @Test
    void getAllBookingShouldReturnEmptyListWithEmptyState() {
        List<BookingDto> result = bookingService.getAllBooking(booker.getId(), BookingState.ALL);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllItemBookingShouldReturnAllBookingsForOwner() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingRequestDto request = createBookingRequestDto(availableItem.getId(), start, end);
        bookingService.addBooking(request);

        List<BookingDto> result = bookingService.getAllItemBooking(owner.getId(), BookingState.ALL);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllItemBookingShouldThrowValidationExceptionWhenUserNotFound() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.getAllItemBooking(999L, BookingState.ALL));
        assertTrue(exception.getMessage().contains("Пользователь с id:999"));
    }

    @Test
    void getAllItemBookingShouldThrowAccessExceptionWhenUserIsNotOwner() {
        AccessException exception = assertThrows(AccessException.class,
                () -> bookingService.getAllItemBooking(anotherUser.getId(), BookingState.ALL));
        assertEquals("Пользователь не является владельцем вещей", exception.getMessage());
    }

    @Test
    void getAllItemBookingShouldThrowAccessExceptionWhenUserHasNoItems() {
        User userWithoutItems = User.builder()
                .name("No Items User")
                .email("noitems@email.com")
                .build();
        userWithoutItems = userRepository.save(userWithoutItems);

        final User user = userWithoutItems;

        AccessException exception = assertThrows(AccessException.class,
                () -> bookingService.getAllItemBooking(user.getId(), BookingState.ALL));
        assertEquals("Пользователь не является владельцем вещей", exception.getMessage());
    }

    @Test
    void getAllItemBookingShouldHandleAllBookingStateValues() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(booking);

        for (BookingState state : BookingState.values()) {
            try {
                List<BookingDto> result = bookingService.getAllItemBooking(owner.getId(), state);
                assertNotNull(result);
            } catch (IllegalArgumentException e) {
                fail("BookingState value " + state + " is not handled properly in getAllItemBooking: " + e.getMessage());
            } catch (AccessException | ValidationException ignored) {
                // Игнорируем для целей теста
            }
        }
    }

    @Test
    void getAllItemBookingShouldReturnEmptyListWithEmptyState() {
        List<BookingDto> result = bookingService.getAllItemBooking(owner.getId(), BookingState.ALL);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllItemBookingShouldReturnApprovedBookingsWithApprovedState() {
        LocalDateTime now = LocalDateTime.now();

        Booking approvedBooking = Booking.builder()
                .start(now.plusHours(1))
                .end(now.plusHours(2))
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(approvedBooking);

        Booking waitingBooking = Booking.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .item(availableItem)
                .booker(anotherUser)
                .status(BookingStatus.WAITING)
                .build();
        bookingRepository.save(waitingBooking);

        List<BookingDto> result = bookingService.getAllItemBooking(owner.getId(), BookingState.APPROVED);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(approvedBooking.getId(), result.getFirst().getId());
        assertEquals("APPROVED", result.getFirst().getStatus());
    }

    @Test
    void hasUserFinishedBookingForItemShouldReturnTrueWhenUserHasFinishedBooking() {
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now().minusDays(1);

        Booking booking = Booking.builder()
                .start(start)
                .end(end)
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(booking);

        boolean result = bookingService.hasUserFinishedBookingForItem(booker.getId(), availableItem.getId());

        assertTrue(result);
    }

    @Test
    void hasUserFinishedBookingForItemShouldReturnFalseWhenUserHasNoFinishedBooking() {
        boolean result = bookingService.hasUserFinishedBookingForItem(booker.getId(), availableItem.getId());

        assertFalse(result);
    }

    @Test
    void getLastBookingForItemShouldReturnMostRecentPastBooking() {
        LocalDateTime now = LocalDateTime.now();

        Booking booking1 = Booking.builder()
                .start(now.minusDays(3))
                .end(now.minusDays(2))
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(booking1);

        Booking booking2 = Booking.builder()
                .start(now.minusDays(1))
                .end(now.minusHours(12))
                .item(availableItem)
                .booker(anotherUser)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(booking2);

        Booking booking3 = Booking.builder()
                .start(now.minusDays(2))
                .end(now.minusDays(1).minusHours(12))
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(booking3);

        BookingShortDto result = bookingService.getLastBookingForItem(availableItem.getId());

        assertNotNull(result);
        assertEquals(booking2.getId(), result.getId());
        assertEquals(anotherUser.getId(), result.getBookerId());
    }

    @Test
    void getLastBookingForItemShouldReturnNullWhenNoLastBooking() {
        BookingShortDto result = bookingService.getLastBookingForItem(availableItem.getId());

        assertNull(result);
    }

    @Test
    void getLastBookingForItemShouldReturnNullWithOnlyWaitingOrRejectedPastBookings() {
        LocalDateTime now = LocalDateTime.now();

        Booking waitingBooking = Booking.builder()
                .start(now.minusDays(2))
                .end(now.minusDays(1))
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        bookingRepository.save(waitingBooking);

        Booking rejectedBooking = Booking.builder()
                .start(now.minusHours(3))
                .end(now.minusHours(2))
                .item(availableItem)
                .booker(anotherUser)
                .status(BookingStatus.REJECTED)
                .build();
        bookingRepository.save(rejectedBooking);

        BookingShortDto result = bookingService.getLastBookingForItem(availableItem.getId());

        assertNull(result);
    }

    @Test
    void getNextBookingForItemShouldReturnEarliestFutureBooking() {
        LocalDateTime now = LocalDateTime.now();

        Booking booking1 = Booking.builder()
                .start(now.plusDays(2))
                .end(now.plusDays(3))
                .item(availableItem)
                .booker(anotherUser)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(booking1);

        Booking booking2 = Booking.builder()
                .start(now.plusHours(2))
                .end(now.plusHours(3))
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(booking2);

        Booking booking3 = Booking.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(1).plusHours(6))
                .item(availableItem)
                .booker(anotherUser)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(booking3);

        BookingShortDto result = bookingService.getNextBookingForItem(availableItem.getId());

        assertNotNull(result);
        assertEquals(booking2.getId(), result.getId());
        assertEquals(booker.getId(), result.getBookerId());
    }

    @Test
    void getNextBookingForItemShouldReturnNullWhenNoNextBooking() {
        BookingShortDto result = bookingService.getNextBookingForItem(availableItem.getId());

        assertNull(result);
    }

    @Test
    void getNextBookingForItemShouldReturnNullWithOnlyWaitingOrRejectedStatus() {
        LocalDateTime now = LocalDateTime.now();

        Booking waitingBooking = Booking.builder()
                .start(now.plusHours(1))
                .end(now.plusHours(2))
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        bookingRepository.save(waitingBooking);

        Booking rejectedBooking = Booking.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .item(availableItem)
                .booker(anotherUser)
                .status(BookingStatus.REJECTED)
                .build();
        bookingRepository.save(rejectedBooking);

        BookingShortDto result = bookingService.getNextBookingForItem(availableItem.getId());

        assertNull(result);
    }

    @Test
    void getNextBookingForItemShouldReturnNullWhenStartIsNowOrInPast() {
        LocalDateTime now = LocalDateTime.now();

        Booking startsNow = Booking.builder()
                .start(now)
                .end(now.plusHours(2))
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(startsNow);

        Booking startedInPast = Booking.builder()
                .start(now.minusHours(1))
                .end(now.plusHours(1))
                .item(availableItem)
                .booker(anotherUser)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(startedInPast);

        BookingShortDto result = bookingService.getNextBookingForItem(availableItem.getId());

        assertNull(result);
    }

    @Test
    void getNextBookingForItemShouldReturnCorrectlyForDifferentItems() {
        LocalDateTime now = LocalDateTime.now();

        Item secondItem = Item.builder()
                .name("Перфоратор")
                .description("Мощный перфоратор")
                .available(true)
                .owner(owner)
                .build();
        secondItem = itemRepository.save(secondItem);

        Booking booking1 = Booking.builder()
                .start(now.plusHours(1))
                .end(now.plusHours(2))
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(booking1);

        Booking booking2 = Booking.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .item(secondItem)
                .booker(anotherUser)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(booking2);

        BookingShortDto result1 = bookingService.getNextBookingForItem(availableItem.getId());
        assertNotNull(result1);
        assertEquals(booking1.getId(), result1.getId());
        assertEquals(booker.getId(), result1.getBookerId());

        BookingShortDto result2 = bookingService.getNextBookingForItem(secondItem.getId());
        assertNotNull(result2);
        assertEquals(booking2.getId(), result2.getId());
        assertEquals(anotherUser.getId(), result2.getBookerId());

        assertNotEquals(result1.getId(), result2.getId());
    }
}