package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemNewRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceImplTest {
    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemService itemService;

    private User user;
    private User anotherUser;
    private User thirdUser;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();

        user = User.builder()
                .name("User")
                .email("user@email.com")
                .build();
        user = userRepository.save(user);

        anotherUser = User.builder()
                .name("Another User")
                .email("another@email.com")
                .build();
        anotherUser = userRepository.save(anotherUser);

        thirdUser = User.builder()
                .name("Third User")
                .email("third@email.com")
                .build();
        thirdUser = userRepository.save(thirdUser);
    }

    @Test
    void createRequest() {
        ItemNewRequestDto requestDto = new ItemNewRequestDto();
        requestDto.setDescription("Нужна дрель для ремонта");

        ItemRequestDto result = itemRequestService.createRequest(user.getId(), requestDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Нужна дрель для ремонта", result.getDescription());
        assertNotNull(result.getCreated());
    }

    @Test
    void createRequestWithEmptyDescriptionShouldThrowException() {
        ItemNewRequestDto requestDto = new ItemNewRequestDto();
        requestDto.setDescription("");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemRequestService.createRequest(user.getId(), requestDto));
        assertEquals("Описание запроса не может быть пустым", exception.getMessage());
    }

    @Test
    void createRequestWithNullDescriptionShouldThrowException() {
        ItemNewRequestDto requestDto = new ItemNewRequestDto();
        requestDto.setDescription(null);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemRequestService.createRequest(user.getId(), requestDto));
        assertEquals("Описание запроса не может быть пустым", exception.getMessage());
    }

    @Test
    void getRequestById() {
        ItemNewRequestDto requestDto = new ItemNewRequestDto();
        requestDto.setDescription("Нужна дрель для ремонта");
        ItemRequestDto created = itemRequestService.createRequest(user.getId(), requestDto);

        ItemRequestDto result = itemRequestService.getRequestById(user.getId(), created.getId());

        assertNotNull(result);
        assertEquals(created.getId(), result.getId());
        assertEquals("Нужна дрель для ремонта", result.getDescription());
    }

    @Test
    void getRequestByIdWithItems() {
        ItemNewRequestDto requestDto = new ItemNewRequestDto();
        requestDto.setDescription("Нужна дрель для ремонта");
        ItemRequestDto created = itemRequestService.createRequest(user.getId(), requestDto);

        Item item = Item.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .owner(anotherUser)
                .request(itemRequestRepository.findById(created.getId()).orElseThrow())
                .build();
        itemRepository.save(item);

        ItemRequestDto result = itemRequestService.getRequestById(user.getId(), created.getId());

        assertNotNull(result);
        assertEquals(created.getId(), result.getId());
        assertEquals(1, result.getItems().size());
        assertEquals("Дрель", result.getItems().getFirst().getName());
    }

    @Test
    void getRequestByIdWhenRequestNotFoundShouldThrowException() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(user.getId(), 999L));
        assertEquals("Запрос с ID 999 не найден", exception.getMessage());
    }

    @Test
    void getRequestEntityById() {
        ItemRequest request = ItemRequest.builder()
                .description("Нужна дрель")
                .requestor(user)
                .created(LocalDateTime.now())
                .build();
        request = itemRequestRepository.save(request);

        ItemRequest result = itemRequestService.getRequestEntityById(request.getId());

        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
        assertEquals("Нужна дрель", result.getDescription());
    }


    @Test
    void getUserRequests() {
        ItemNewRequestDto requestDto1 = new ItemNewRequestDto();
        requestDto1.setDescription("Нужна дрель");
        itemRequestService.createRequest(user.getId(), requestDto1);

        ItemNewRequestDto requestDto2 = new ItemNewRequestDto();
        requestDto2.setDescription("Нужен молоток");
        itemRequestService.createRequest(user.getId(), requestDto2);

        Pageable pageable = PageRequest.of(0, 10);
        List<ItemRequestDto> result = itemRequestService.getUserRequests(user.getId(), pageable);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getDescription().equals("Нужна дрель")));
        assertTrue(result.stream().anyMatch(r -> r.getDescription().equals("Нужен молоток")));
    }

    @Test
    void getAllRequests() {
        ItemNewRequestDto requestDto = new ItemNewRequestDto();
        requestDto.setDescription("Нужна дрель");
        itemRequestService.createRequest(anotherUser.getId(), requestDto);

        Pageable pageable = PageRequest.of(0, 10);
        List<ItemRequestDto> result = itemRequestService.getAllRequests(user.getId(), pageable);

        assertEquals(1, result.size());
        assertEquals("Нужна дрель", result.getFirst().getDescription());
    }

    @Test
    void getAllRequestsShouldNotIncludeOwnRequests() {
        ItemNewRequestDto ownRequest = new ItemNewRequestDto();
        ownRequest.setDescription("Мой запрос");
        itemRequestService.createRequest(user.getId(), ownRequest);

        ItemNewRequestDto otherRequest = new ItemNewRequestDto();
        otherRequest.setDescription("Чужой запрос");
        itemRequestService.createRequest(anotherUser.getId(), otherRequest);

        Pageable pageable = PageRequest.of(0, 10);
        List<ItemRequestDto> result = itemRequestService.getAllRequests(user.getId(), pageable);

        assertEquals(1, result.size());
        assertEquals("Чужой запрос", result.getFirst().getDescription());
    }

    @Test
    void getAllRequestsWithItems() {
        ItemNewRequestDto requestDto = new ItemNewRequestDto();
        requestDto.setDescription("Нужна дрель");
        ItemRequestDto created = itemRequestService.createRequest(anotherUser.getId(), requestDto);

        ItemRequest request = itemRequestRepository.findById(created.getId()).orElseThrow();
        Item item = Item.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .owner(thirdUser)
                .request(request)
                .build();
        itemRepository.save(item);

        Pageable pageable = PageRequest.of(0, 10);
        List<ItemRequestDto> result = itemRequestService.getAllRequests(user.getId(), pageable);

        assertEquals(1, result.size());
        assertEquals("Нужна дрель", result.getFirst().getDescription());
        assertEquals(1, result.getFirst().getItems().size());
        assertEquals("Дрель", result.getFirst().getItems().getFirst().getName());
    }

    @Test
    void createRequestWithNullRequestDtoShouldThrowException() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemRequestService.createRequest(user.getId(), null));
        assertEquals("Запрос не может быть null", exception.getMessage());
    }

    @Test
    void getUserRequestsWhenNoRequestsExist() {
        Pageable pageable = PageRequest.of(0, 10);
        List<ItemRequestDto> result = itemRequestService.getUserRequests(user.getId(), pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    void getAllRequestsWhenNoRequestsExist() {
        Pageable pageable = PageRequest.of(0, 10);
        List<ItemRequestDto> result = itemRequestService.getAllRequests(user.getId(), pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }
}