package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsAndBookingDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImplTest {
    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;


    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .name("Владелец")
                .email("owner@test.com")
                .build();
        owner = userRepository.save(owner);

        booker = User.builder()
                .name("Бронирующий")
                .email("booker@test.com")
                .build();
        booker = userRepository.save(booker);

        item = Item.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .owner(owner)
                .build();
        item = itemRepository.save(item);
    }

    @Test
    void createShouldCreateNewItem() {
        Item newItem = Item.builder()
                .name("New Item")
                .description("New Description")
                .available(true)
                .build();

        Item result = itemService.create(newItem, owner.getId());

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("New Item", result.getName());
        assertEquals("New Description", result.getDescription());
        assertTrue(result.getAvailable());
        assertEquals(owner.getId(), result.getOwner().getId());
    }

    @Test
    void createShouldOverrideOwnerWhenItemAlreadyHasOwner() {
        User otherUser = User.builder()
                .name("Other User")
                .email("other@test.com")
                .build();
        userRepository.save(otherUser);

        Item newItem = Item.builder()
                .name("New Item")
                .description("New Description")
                .available(true)
                .owner(otherUser)
                .build();

        Item result = itemService.create(newItem, owner.getId());

        assertEquals(owner.getId(), result.getOwner().getId());
        assertNotEquals(otherUser.getId(), result.getOwner().getId());
    }

    @Test
    void updateShouldUpdateItem() {
        Item updateData = Item.builder()
                .id(item.getId())
                .name("Updated Item")
                .description("Updated Description")
                .available(false)
                .build();

        Item result = itemService.update(updateData, owner.getId());

        assertEquals("Updated Item", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    void updateShouldOnlyUpdateNameWhenOnlyNameProvided() {
        Item updateData = Item.builder()
                .id(item.getId())
                .name("Updated Name Only")
                .description(null)
                .available(null)
                .build();

        Item result = itemService.update(updateData, owner.getId());

        assertEquals("Updated Name Only", result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getAvailable(), result.getAvailable());
    }

    @Test
    void updateShouldKeepOriginalNameWhenNameIsNull() {
        String originalName = item.getName();

        Item updateData = Item.builder()
                .id(item.getId())
                .name(null)
                .description("Новое описание")
                .build();

        Item result = itemService.update(updateData, owner.getId());

        assertEquals(originalName, result.getName());
        assertEquals("Новое описание", result.getDescription());
    }

    @Test
    void updateShouldThrowExceptionWhenUserIsNotOwner() {
        Item updateData = Item.builder()
                .id(item.getId())
                .name("Updated Item")
                .build();

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.update(updateData, booker.getId()));

        assertEquals("Редактировать вещь может только владелец", exception.getMessage());
    }

    @Test
    void getByIdShouldReturnItem() {
        Item result = itemService.getById(item.getId());

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
    }

    @Test
    void getByIdShouldThrowExceptionWhenNotFound() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.getById(999L));
        assertEquals("Вещь с ID 999 не найдена", exception.getMessage());
    }

    @Test
    void deleteShouldDeleteItem() {
        itemService.delete(item.getId(), owner.getId());

        assertFalse(itemRepository.existsById(item.getId()));
    }

    @Test
    void deleteShouldThrowExceptionWhenNotOwner() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.delete(item.getId(), booker.getId()));
        assertEquals("Удалять вещь может только владелец", exception.getMessage());
    }

    @Test
    void getAllByOwnerShouldReturnItems() {
        Item secondItem = Item.builder()
                .name("Second Item")
                .description("Second Description")
                .available(true)
                .owner(owner)
                .build();
        itemRepository.save(secondItem);

        List<Item> result = itemService.getAllByOwner(owner.getId(), 0, 10);

        assertEquals(2, result.size());
    }

    @Test
    void getAllByOwnerShouldThrowExceptionWhenInvalidPagination() {
        Item secondItem = Item.builder()
                .name("Second Item")
                .description("Second Description")
                .available(true)
                .owner(owner)
                .build();
        itemRepository.save(secondItem);

        ValidationException exception1 = assertThrows(ValidationException.class,
                () -> itemService.getAllByOwner(owner.getId(), -1, 10));
        assertEquals("Параметр 'from' не может быть отрицательным", exception1.getMessage());

        ValidationException exception2 = assertThrows(ValidationException.class,
                () -> itemService.getAllByOwner(owner.getId(), 0, 0));
        assertEquals("Параметр 'size' должен быть положительным", exception2.getMessage());

        ValidationException exception3 = assertThrows(ValidationException.class,
                () -> itemService.getAllByOwner(owner.getId(), 0, -5));
        assertEquals("Параметр 'size' должен быть положительным", exception3.getMessage());

        assertDoesNotThrow(() -> itemService.getAllByOwner(owner.getId(), 0, 5));
    }

    @Test
    void searchShouldReturnMatchingItems() {
        Item searchableItem = Item.builder()
                .name("Hammer")
                .description("Big hammer for construction")
                .available(true)
                .owner(owner)
                .build();
        itemRepository.save(searchableItem);

        List<Item> result = itemService.search("hammer", 0, 10);

        assertEquals(1, result.size());
        assertEquals("Hammer", result.getFirst().getName());
    }

    @Test
    void searchShouldReturnEmptyListWhenEmptyText() {
        Item searchableItem = Item.builder()
                .name("Hammer")
                .description("Big hammer for construction")
                .available(true)
                .owner(owner)
                .build();
        itemRepository.save(searchableItem);

        List<Item> result1 = itemService.search("", 0, 10);
        assertTrue(result1.isEmpty());

        List<Item> result2 = itemService.search("   ", 0, 10);
        assertTrue(result2.isEmpty());

        List<Item> result3 = itemService.search(null, 0, 10);
        assertTrue(result3.isEmpty());
    }

    @Test
    void searchShouldThrowExceptionWhenInvalidPagination() {
        ValidationException exception1 = assertThrows(ValidationException.class,
                () -> itemService.search("test", -1, 10));
        assertEquals("Параметр 'from' не может быть отрицательным", exception1.getMessage());

        ValidationException exception2 = assertThrows(ValidationException.class,
                () -> itemService.search("test", 0, 0));
        assertEquals("Параметр 'size' должен быть положительным", exception2.getMessage());

        ValidationException exception3 = assertThrows(ValidationException.class,
                () -> itemService.search("test", 0, -5));
        assertEquals("Параметр 'size' должен быть положительным", exception3.getMessage());
    }

    @Test
    void getItemWithCommentsAndBookingsShouldReturnItemWithComments() {
        Comment comment = Comment.builder()
                .text("всё ок")
                .item(item)
                .author(booker)
                .created(LocalDateTime.now())
                .build();
        commentRepository.save(comment);

        ItemWithCommentsAndBookingDto result = itemService.getItemWithCommentsAndBookings(item.getId(), owner.getId());

        assertNotNull(result);
        assertEquals(1, result.getComments().size());
        assertEquals("всё ок", result.getComments().getFirst().getText());
    }

    @Test
    void getItemWithCommentsAndBookingsShouldNotReturnBookingsWhenUserIsNotOwner() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(booking);

        ItemWithCommentsAndBookingDto result = itemService.getItemWithCommentsAndBookings(
                item.getId(), booker.getId());

        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }

    @Test
    void getItemWithCommentsAndBookingsShouldReturnNullBookingsWhenNoBookingsExist() {
        ItemWithCommentsAndBookingDto result = itemService.getItemWithCommentsAndBookings(
                item.getId(), owner.getId());

        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }

    @Test
    void getItemsWithCommentsAndBookingsByOwnerShouldReturnItems() {
        Item secondItem = Item.builder()
                .name("Вещь2")
                .description("Описание2")
                .available(true)
                .owner(owner)
                .build();
        itemRepository.save(secondItem);

        List<ItemWithCommentsAndBookingDto> result =
                itemService.getItemsWithCommentsAndBookingsByOwner(owner.getId(), 0, 10);

        assertEquals(2, result.size());
        result.forEach(itemDto -> {
            assertNotNull(itemDto.getComments());
        });
    }

    @Test
    void addCommentShouldSuccessfullyAddCommentWhenUserHasFinishedBooking() {
        Booking finishedBooking = Booking.builder()
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(finishedBooking);

        CommentDto commentDto = CommentDto.builder()
                .text("Отличная дрель! Всё работает прекрасно.")
                .build();

        CommentDto result = itemService.addComment(booker.getId(), item.getId(), commentDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Отличная дрель! Всё работает прекрасно.", result.getText());
        assertEquals(booker.getName(), result.getAuthorName());
        assertNotNull(result.getCreated());

        List<Comment> comments = commentRepository.findByItemId(item.getId());
        assertEquals(1, comments.size());
        assertEquals("Отличная дрель! Всё работает прекрасно.", comments.getFirst().getText());
    }

    @Test
    void addCommentShouldThrowExceptionWhenUserHasNoFinishedBookings() {
        CommentDto commentDto = CommentDto.builder()
                .text("Отличная дрель! Всё работает прекрасно.")
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addComment(booker.getId(), item.getId(), commentDto));

        assertTrue(exception.getMessage().contains("комментарий") ||
                exception.getMessage().contains("бронирование"));
    }

    @Test
    void addCommentShouldThrowNotFoundExceptionWhenItemNotFound() {
        CommentDto commentDto = CommentDto.builder()
                .text("Комментарий")
                .build();

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.addComment(booker.getId(), 999L, commentDto));

        assertTrue(exception.getMessage().contains("не найдена"));
    }

    @Test
    void getItemsByRequestIdShouldReturnItemsWhenRequestExists() {
        ItemRequest request = ItemRequest.builder()
                .description("Нужна дрель")
                .requestor(booker)
                .created(LocalDateTime.now())
                .build();
        itemRequestRepository.save(request);

        item.setRequest(request);
        itemRepository.save(item);

        List<ItemDto> result = itemService.getItemsByRequestId(request.getId());

        assertEquals(1, result.size());
        assertEquals(item.getId(), result.getFirst().getId());
    }

    @Test
    void getItemsByRequestIdShouldReturnEmptyListWhenNoItemsForRequest() {
        ItemRequest request = ItemRequest.builder()
                .description("Нужна дрель")
                .requestor(booker)
                .created(LocalDateTime.now())
                .build();
        itemRequestRepository.save(request);

        List<ItemDto> result = itemService.getItemsByRequestId(request.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void getItemsByRequestIdsShouldReturnEmptyMapWhenRequestIdsIsNull() {
        Map<Long, List<ItemDto>> result = itemService.getItemsByRequestIds(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getItemsByRequestIdsShouldReturnEmptyMapWhenRequestIdsIsEmpty() {
        ItemRequest request = ItemRequest.builder()
                .description("Запрос")
                .requestor(booker)
                .created(LocalDateTime.now())
                .build();
        itemRequestRepository.save(request);

        item.setRequest(request);
        itemRepository.save(item);

        Map<Long, List<ItemDto>> result = itemService.getItemsByRequestIds(List.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    void getItemsByRequestIdsShouldReturnEmptyMapWhenNoItemsForRequests() {
        ItemRequest request1 = ItemRequest.builder()
                .description("Запрос 1")
                .requestor(booker)
                .created(LocalDateTime.now())
                .build();
        ItemRequest request2 = ItemRequest.builder()
                .description("Запрос 2")
                .requestor(booker)
                .created(LocalDateTime.now())
                .build();
        itemRequestRepository.saveAll(List.of(request1, request2));

        Map<Long, List<ItemDto>> result = itemService.getItemsByRequestIds(
                List.of(request1.getId(), request2.getId()));

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }
}