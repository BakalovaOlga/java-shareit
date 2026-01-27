package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CommentMapperTest {

    private CommentMapper commentMapper;
    private CommentDto commentDto;
    private Item item;
    private User author;

    @BeforeEach
    void setUp() {
        commentMapper = new CommentMapper() {
            @Override
            public CommentDto toDto(Comment comment) {
                if (comment == null) {
                    return null;
                }
                return CommentDto.builder()
                        .id(comment.getId())
                        .text(comment.getText())
                        .authorName(comment.getAuthor() != null ? comment.getAuthor().getName() : null)
                        .created(comment.getCreated())
                        .build();
            }

            @Override
            public List<CommentDto> toDtoList(List<Comment> comments) {
                if (comments == null) {
                    return null;
                }
                return comments.stream()
                        .map(this::toDto)
                        .toList();
            }
        };

        commentDto = CommentDto.builder()
                .id(1L)
                .text("Test comment text")
                .authorName("Test Author")
                .build();

        item = Item.builder()
                .id(10L)
                .name("Test Item")
                .build();

        author = User.builder()
                .id(100L)
                .name("Author Name")
                .email("author@example.com")
                .build();
    }

    @Test
    void shouldCreateCommentWithTextOnly() {
        CommentDto minimalDto = CommentDto.builder()
                .text("Minimal comment")
                .build();

        Comment comment = commentMapper.toComment(minimalDto, item, author);

        assertNotNull(comment);
        assertThat(comment.getText()).isEqualTo("Minimal comment");
        assertThat(comment.getItem()).isSameAs(item);
        assertThat(comment.getAuthor()).isSameAs(author);
        assertNotNull(comment.getCreated());
    }

    @Test
    void shouldThrowExceptionWhenDtoIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> commentMapper.toComment(null, item, author)
        );

        assertThat(exception.getMessage()).contains("commentDto");
    }

    @Test
    void shouldCreateMultipleCommentsWithDifferentTimestamps() throws InterruptedException {
        Comment comment1 = commentMapper.toComment(commentDto, item, author);
        Thread.sleep(10); // Небольшая задержка
        Comment comment2 = commentMapper.toComment(commentDto, item, author);

        assertThat(comment1.getCreated()).isBefore(comment2.getCreated());
        assertThat(comment1.getCreated()).isNotEqualTo(comment2.getCreated());
    }
}