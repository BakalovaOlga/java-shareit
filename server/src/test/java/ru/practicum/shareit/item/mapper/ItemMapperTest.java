package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ItemMapperTest {

    @Autowired
    private ItemMapper itemMapper;

    private User owner;
    private ItemRequest request;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@example.com")
                .build();

        request = ItemRequest.builder()
                .id(10L)
                .description("Need a drill")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .owner(owner)
                .request(request)
                .build();

        itemDto = ItemDto.builder()
                .id(2L)
                .name("Hammer")
                .description("Construction hammer")
                .available(false)
                .ownerId(owner.getId())
                .requestId(request.getId())
                .build();
    }

    @Test
    void toDto() {
        ItemDto result = itemMapper.toDto(item);

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getAvailable(), result.getAvailable());
        assertEquals(owner.getId(), result.getOwnerId());
        assertEquals(request.getId(), result.getRequestId());
    }

    @Test
    void toDtoWithNullItemShouldReturnNull() {
        ItemDto result = itemMapper.toDto(null);

        assertNull(result);
    }

    @Test
    void toDtoWithNullOwnerAndRequest() {
        Item itemWithoutBoth = Item.builder()
                .id(5L)
                .name("Simple item")
                .description("Test")
                .available(true)
                .owner(null)
                .request(null)
                .build();

        ItemDto result = itemMapper.toDto(itemWithoutBoth);

        assertNotNull(result);
        assertNull(result.getOwnerId());
        assertNull(result.getRequestId());
    }

    @Test
    void toEntity() {
        Item result = itemMapper.toEntity(itemDto);

        assertNotNull(result);
        assertEquals(itemDto.getId(), result.getId());
        assertEquals(itemDto.getName(), result.getName());
        assertEquals(itemDto.getDescription(), result.getDescription());
        assertEquals(itemDto.getAvailable(), result.getAvailable());
        assertNull(result.getOwner());
        assertNull(result.getRequest());
    }

    @Test
    void toEntityWithNullDtoShouldReturnNull() {
        Item result = itemMapper.toEntity(null);

        assertNull(result);
    }


    @Test
    void updateItem() {
        Item existingItem = Item.builder()
                .id(1L)
                .name("Old Name")
                .description("Old Description")
                .available(true)
                .owner(owner)
                .request(request)
                .build();

        ItemDto updateDto = ItemDto.builder()
                .id(999L)
                .name("New Name")
                .description("New Description")
                .available(false)
                .ownerId(999L)
                .requestId(999L)
                .build();

        itemMapper.updateItem(updateDto, existingItem);

        assertEquals(1L, existingItem.getId());
        assertEquals("New Name", existingItem.getName());
        assertEquals("New Description", existingItem.getDescription());
        assertFalse(existingItem.getAvailable());
        assertEquals(owner, existingItem.getOwner());
        assertEquals(request, existingItem.getRequest());
    }

    @Test
    void updateItemWithNullNameShouldNotUpdateName() {
        Item existingItem = Item.builder()
                .id(1L)
                .name("Original Name")
                .description("Original Description")
                .available(true)
                .owner(owner)
                .build();

        ItemDto updateDto = ItemDto.builder()
                .name(null)
                .description("Updated Description")
                .available(false)
                .build();

        itemMapper.updateItem(updateDto, existingItem);

        assertEquals("Original Name", existingItem.getName());
        assertEquals("Updated Description", existingItem.getDescription());
        assertFalse(existingItem.getAvailable());
    }

    @Test
    void updateItemWithNullDtoShouldDoNothing() {
        Item existingItem = Item.builder()
                .id(1L)
                .name("Original Name")
                .description("Original Description")
                .available(true)
                .owner(owner)
                .build();

        Item originalState = Item.builder()
                .id(existingItem.getId())
                .name(existingItem.getName())
                .description(existingItem.getDescription())
                .available(existingItem.getAvailable())
                .owner(existingItem.getOwner())
                .request(existingItem.getRequest())
                .build();

        itemMapper.updateItem(null, existingItem);

        assertEquals(originalState.getId(), existingItem.getId());
        assertEquals(originalState.getName(), existingItem.getName());
        assertEquals(originalState.getDescription(), existingItem.getDescription());
        assertEquals(originalState.getAvailable(), existingItem.getAvailable());
        assertEquals(originalState.getOwner(), existingItem.getOwner());
    }

    @Test
    void toDtoList() {
        Item item1 = Item.builder()
                .id(1L)
                .name("Item 1")
                .description("Description 1")
                .available(true)
                .owner(owner)
                .build();

        Item item2 = Item.builder()
                .id(2L)
                .name("Item 2")
                .description("Description 2")
                .available(false)
                .owner(owner)
                .build();

        List<Item> items = List.of(item1, item2);

        List<ItemDto> result = itemMapper.toDtoList(items);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(item1.getId(), result.get(0).getId());
        assertEquals(item2.getId(), result.get(1).getId());
        assertEquals(item1.getName(), result.get(0).getName());
        assertEquals(item2.getName(), result.get(1).getName());
        assertEquals(owner.getId(), result.get(0).getOwnerId());
        assertEquals(owner.getId(), result.get(1).getOwnerId());
    }


    @Test
    void toDtoListWithNullListShouldReturnNull() {
        List<ItemDto> result = itemMapper.toDtoList(null);

        assertNull(result);
    }

    @Test
    void updateItemWithEmptyStringNameShouldUpdateNameToEmpty() {
        Item existingItem = Item.builder()
                .id(1L)
                .name("Original Name")
                .description("Original Description")
                .available(true)
                .owner(owner)
                .build();

        ItemDto updateDto = ItemDto.builder()
                .name("")
                .description("Updated Description")
                .available(false)
                .build();

        itemMapper.updateItem(updateDto, existingItem);

        assertEquals("", existingItem.getName());
        assertEquals("Updated Description", existingItem.getDescription());
        assertFalse(existingItem.getAvailable());
    }


    @Test
    void toDtoWithEmptyStringsShouldMapCorrectly() {
        Item itemWithEmptyFields = Item.builder()
                .id(1L)
                .name("")
                .description("")
                .available(true)
                .owner(owner)
                .request(request)
                .build();

        ItemDto result = itemMapper.toDto(itemWithEmptyFields);

        assertNotNull(result);
        assertEquals("", result.getName());
        assertEquals("", result.getDescription());
    }
}