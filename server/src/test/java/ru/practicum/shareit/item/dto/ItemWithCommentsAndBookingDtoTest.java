package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemWithCommentsAndBookingDtoTest {

    @Autowired
    private JacksonTester<ItemWithCommentsAndBookingDto> json;

    @Test
    void testSerialize() throws IOException {
        LocalDateTime comment1Time = LocalDateTime.of(2024, 12, 1, 10, 0, 0);
        LocalDateTime comment2Time = LocalDateTime.of(2024, 12, 2, 10, 0, 0);

        BookingShortDto lastBooking = BookingShortDto.builder()
                .id(1L)
                .bookerId(100L)
                .build();

        BookingShortDto nextBooking = BookingShortDto.builder()
                .id(2L)
                .bookerId(101L)
                .build();

        CommentDto comment1 = CommentDto.builder()
                .id(10L)
                .text("Great item!")
                .authorName("John Doe")
                .created(comment1Time)
                .build();

        CommentDto comment2 = CommentDto.builder()
                .id(11L)
                .text("Good quality")
                .authorName("Jane Smith")
                .created(comment2Time)
                .build();

        ItemWithCommentsAndBookingDto dto = ItemWithCommentsAndBookingDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(List.of(comment1, comment2))
                .ownerId(200L)
                .requestId(300L)
                .build();

        JsonContent<ItemWithCommentsAndBookingDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Test Item");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Test Description");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.ownerId").isEqualTo(200);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(300);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.bookerId").isEqualTo(100);
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(2);
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.bookerId").isEqualTo(101);
        assertThat(result).extractingJsonPathArrayValue("$.comments").hasSize(2);
        assertThat(result).extractingJsonPathNumberValue("$.comments[0].id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.comments[0].text").isEqualTo("Great item!");
        assertThat(result).extractingJsonPathStringValue("$.comments[0].authorName").isEqualTo("John Doe");
        assertThat(result).extractingJsonPathStringValue("$.comments[0].created")
                .isEqualTo("2024-12-01T10:00:00");
        assertThat(result).extractingJsonPathNumberValue("$.comments[1].id").isEqualTo(11);
        assertThat(result).extractingJsonPathStringValue("$.comments[1].text").isEqualTo("Good quality");
        assertThat(result).extractingJsonPathStringValue("$.comments[1].authorName").isEqualTo("Jane Smith");
        assertThat(result).extractingJsonPathStringValue("$.comments[1].created")
                .isEqualTo("2024-12-02T10:00:00");
    }

    @Test
    void testSerializeWithNullFields() throws IOException {
        ItemWithCommentsAndBookingDto dto = ItemWithCommentsAndBookingDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .lastBooking(null)
                .nextBooking(null)
                .comments(null)
                .ownerId(null)
                .requestId(null)
                .build();

        JsonContent<ItemWithCommentsAndBookingDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Test Item");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Test Description");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathValue("$.lastBooking").isNull();
        assertThat(result).extractingJsonPathValue("$.nextBooking").isNull();
        assertThat(result).extractingJsonPathValue("$.comments").isNull();
        assertThat(result).extractingJsonPathValue("$.ownerId").isNull();
        assertThat(result).extractingJsonPathValue("$.requestId").isNull();
    }

    @Test
    void testSerializeWithEmptyComments() throws IOException {
        ItemWithCommentsAndBookingDto dto = ItemWithCommentsAndBookingDto.builder()
                .id(1L)
                .name("Test Item")
                .available(true)
                .comments(List.of())
                .build();

        JsonContent<ItemWithCommentsAndBookingDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathArrayValue("$.comments").isEmpty();
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = String.join("",
                "{",
                "\"id\": 1,",
                "\"name\": \"Test Item\",",
                "\"description\": \"Test Description\",",
                "\"available\": true,",
                "\"lastBooking\": {",
                "    \"id\": 1,",
                "    \"bookerId\": 100",
                "},",
                "\"nextBooking\": {",
                "    \"id\": 2,",
                "    \"bookerId\": 101",
                "},",
                "\"comments\": [",
                "    {",
                "        \"id\": 10,",
                "        \"text\": \"Great item!\",",
                "        \"authorName\": \"John Doe\",",
                "        \"created\": \"2024-12-01T10:00:00\"",
                "    },",
                "    {",
                "        \"id\": 11,",
                "        \"text\": \"Good quality\",",
                "        \"authorName\": \"Jane Smith\",",
                "        \"created\": \"2024-12-02T10:00:00\"",
                "    }",
                "],",
                "\"ownerId\": 200,",
                "\"requestId\": 300",
                "}"
        );

        ItemWithCommentsAndBookingDto dto = json.parseObject(jsonContent);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Test Item");
        assertThat(dto.getDescription()).isEqualTo("Test Description");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getOwnerId()).isEqualTo(200L);
        assertThat(dto.getRequestId()).isEqualTo(300L);

        //lastBooking
        assertThat(dto.getLastBooking()).isNotNull();
        assertThat(dto.getLastBooking().getId()).isEqualTo(1L);
        assertThat(dto.getLastBooking().getBookerId()).isEqualTo(100L);

        //nextBooking
        assertThat(dto.getNextBooking()).isNotNull();
        assertThat(dto.getNextBooking().getId()).isEqualTo(2L);
        assertThat(dto.getNextBooking().getBookerId()).isEqualTo(101L);

        //comments
        assertThat(dto.getComments()).hasSize(2);
        assertThat(dto.getComments().get(0).getId()).isEqualTo(10L);
        assertThat(dto.getComments().get(0).getText()).isEqualTo("Great item!");
        assertThat(dto.getComments().get(0).getAuthorName()).isEqualTo("John Doe");
        assertThat(dto.getComments().get(0).getCreated()).isEqualTo(LocalDateTime.of(2024, 12, 1, 10, 0, 0));
        assertThat(dto.getComments().get(1).getId()).isEqualTo(11L);
        assertThat(dto.getComments().get(1).getText()).isEqualTo("Good quality");
        assertThat(dto.getComments().get(1).getAuthorName()).isEqualTo("Jane Smith");
        assertThat(dto.getComments().get(1).getCreated()).isEqualTo(LocalDateTime.of(2024, 12, 2, 10, 0, 0));
    }

    @Test
    void testDeserializeWithPartialData() throws IOException {
        String jsonContent = String.join("",
                "{",
                "\"id\": 1,",
                "\"name\": \"Test Item\",",
                "\"available\": true",
                "}"
        );

        ItemWithCommentsAndBookingDto dto = json.parseObject(jsonContent);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Test Item");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getDescription()).isNull();
        assertThat(dto.getLastBooking()).isNull();
        assertThat(dto.getNextBooking()).isNull();
        assertThat(dto.getComments()).isNull();
        assertThat(dto.getOwnerId()).isNull();
        assertThat(dto.getRequestId()).isNull();
    }

    @Test
    void testDeserializeWithNullValues() throws IOException {
        String jsonContent = String.join("",
                "{",
                "\"id\": 1,",
                "\"name\": \"Test Item\",",
                "\"available\": true,",
                "\"lastBooking\": null,",
                "\"nextBooking\": null,",
                "\"comments\": null,",
                "\"ownerId\": null,",
                "\"requestId\": null",
                "}"
        );

        ItemWithCommentsAndBookingDto dto = json.parseObject(jsonContent);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Test Item");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getLastBooking()).isNull();
        assertThat(dto.getNextBooking()).isNull();
        assertThat(dto.getComments()).isNull();
        assertThat(dto.getOwnerId()).isNull();
        assertThat(dto.getRequestId()).isNull();
    }

    @Test
    void testSerializeDeserializeCycle() throws IOException {
        LocalDateTime commentTime = LocalDateTime.of(2024, 12, 1, 10, 0, 0);

        BookingShortDto lastBooking = BookingShortDto.builder()
                .id(1L)
                .bookerId(100L)
                .build();

        BookingShortDto nextBooking = BookingShortDto.builder()
                .id(2L)
                .bookerId(101L)
                .build();

        CommentDto comment = CommentDto.builder()
                .id(10L)
                .text("Great item!")
                .authorName("John Doe")
                .created(commentTime)
                .build();

        ItemWithCommentsAndBookingDto originalDto = ItemWithCommentsAndBookingDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(List.of(comment))
                .ownerId(200L)
                .requestId(300L)
                .build();

        String jsonString = this.json.write(originalDto).getJson();
        ItemWithCommentsAndBookingDto deserializedDto = this.json.parseObject(jsonString);

        assertThat(deserializedDto.getId()).isEqualTo(originalDto.getId());
        assertThat(deserializedDto.getName()).isEqualTo(originalDto.getName());
        assertThat(deserializedDto.getDescription()).isEqualTo(originalDto.getDescription());
        assertThat(deserializedDto.getAvailable()).isEqualTo(originalDto.getAvailable());
        assertThat(deserializedDto.getOwnerId()).isEqualTo(originalDto.getOwnerId());
        assertThat(deserializedDto.getRequestId()).isEqualTo(originalDto.getRequestId());
        assertThat(deserializedDto.getLastBooking().getId()).isEqualTo(originalDto.getLastBooking().getId());
        assertThat(deserializedDto.getNextBooking().getId()).isEqualTo(originalDto.getNextBooking().getId());
        assertThat(deserializedDto.getComments()).hasSize(1);
        assertThat(deserializedDto.getComments().get(0).getText()).isEqualTo(originalDto.getComments().get(0).getText());
        assertThat(deserializedDto.getComments().get(0).getCreated()).isEqualTo(originalDto.getComments().get(0).getCreated());
    }
}