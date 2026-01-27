package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class CommentTest {

    @Test
    void testCommentCreation() {
        LocalDateTime now = LocalDateTime.now();
        User author = User.builder().id(1L).name("Ольга").build();
        Item item = Item.builder().id(1L).name("Гитара").build();

        Comment comment = Comment.builder()
                .id(1L)
                .text("Отличная гитара!")
                .item(item)
                .author(author)
                .created(now)
                .build();

        assertNotNull(comment);
        assertEquals(1L, comment.getId());
        assertEquals("Отличная гитара!", comment.getText());
        assertEquals(item, comment.getItem());
        assertEquals(author, comment.getAuthor());
        assertEquals(now, comment.getCreated());
    }

    @Test
    void shouldHaveCorrectEqualsAndHashCode() {

        Comment comment1 = Comment.builder().id(1L).build();
        Comment comment2 = Comment.builder().id(1L).build();
        Comment comment3 = Comment.builder().id(2L).build();

        assertThat(comment1).isEqualTo(comment2);
        assertThat(comment1).isNotEqualTo(comment3);
        assertThat(comment1).isEqualTo(comment1);
        assertThat(comment1.equals(null)).isFalse();

        assertThat(comment1.hashCode()).isEqualTo(comment2.hashCode());
        assertThat(comment1.hashCode()).isNotEqualTo(comment3.hashCode());
    }

    @Test
    void equalsShouldReturnFalseWhenComparingWithNull() {
        Comment comment = Comment.builder().id(1L).build();

        assertFalse(comment.equals(null));
    }

    @Test
    void equalsShouldReturnFalseWhenComparingCommentWithUser() {
        Comment comment = Comment.builder().id(1L).build();
        User user = User.builder().id(1L).build();

        assertFalse(comment.equals(user));
    }
}