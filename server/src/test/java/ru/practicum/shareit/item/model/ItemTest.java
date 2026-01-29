package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    void testItemCreationWithBuilder() {
        User owner = User.builder().id(1L).name("Ольга").build();
        ItemRequest request = ItemRequest.builder().id(1L).description("Нужна дрель").build();

        Item item = Item.builder()
                .id(1L)
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .owner(owner)
                .request(request)
                .build();

        assertNotNull(item);
        assertEquals(1L, item.getId());
        assertEquals("Дрель", item.getName());
        assertEquals("Мощная дрель", item.getDescription());
        assertTrue(item.getAvailable());
        assertEquals(owner, item.getOwner());
        assertEquals(request, item.getRequest());
    }

    @Test
    void shouldHaveCorrectEqualsAndHashCode() {

        Item item1 = Item.builder().id(1L).build();
        Item item2 = Item.builder().id(1L).build();
        Item item3 = Item.builder().id(2L).build();

        assertThat(item1).isEqualTo(item2);
        assertThat(item1).isNotEqualTo(item3);
        assertThat(item1).isEqualTo(item1);
        assertThat(item1.equals(null)).isFalse();

        assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
        assertThat(item1.hashCode()).isNotEqualTo(item3.hashCode());
    }

    @Test
    void equalsShouldReturnFalseWhenComparingWithNull() {
        Item item = Item.builder().id(1L).build();

        assertFalse(item.equals(null));
    }

    @Test
    void equalsShouldReturnFalseWhenComparingCommentWithUser() {
        Item item = Item.builder().id(1L).build();
        User user = User.builder().id(1L).build();

        assertFalse(item.equals(user));
    }
}