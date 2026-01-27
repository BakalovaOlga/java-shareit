package ru.practicum.shareit.user.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class UserTest {
    @Test
    void shouldHaveCorrectEqualsAndHashCode() {

        User user1 = User.builder().id(1L).build();
        User user2 = User.builder().id(1L).build();
        User user3 = User.builder().id(2L).build();

        assertThat(user1).isEqualTo(user2);
        assertThat(user1).isNotEqualTo(user3);
        assertThat(user1).isEqualTo(user1);
        assertThat(user1.equals(null)).isFalse();

        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
        assertThat(user1.hashCode()).isNotEqualTo(user3.hashCode());
    }

    @Test
    void equalsShouldReturnFalseWhenComparingWithNull() {
        User user = User.builder().id(1L).build();

        assertFalse(user.equals(null));
    }

    @Test
    void equalsShouldReturnFalseWhenComparingCommentWithUser() {
        User user = User.builder().id(1L).build();
        Item item = Item.builder().id(1L).build();

        assertFalse(user.equals(item));
    }
}
