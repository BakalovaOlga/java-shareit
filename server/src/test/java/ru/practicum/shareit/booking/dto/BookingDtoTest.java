package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void testSerialize() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 12, 1, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 2, 10, 0, 0);

        ItemDto item = ItemDto.builder()
                .id(10L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        UserDto booker = UserDto.builder()
                .id(100L)
                .name("Test User")
                .email("test@example.com")
                .build();

        BookingDto dto = BookingDto.builder()
                .id(1L)
                .item(item)
                .booker(booker)
                .start(start)
                .end(end)
                .status("APPROVED")
                .build();

        JsonContent<BookingDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("Test Item");
        assertThat(result).extractingJsonPathStringValue("$.item.description").isEqualTo("Test Description");
        assertThat(result).extractingJsonPathBooleanValue("$.item.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(100);
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("Test User");
        assertThat(result).extractingJsonPathStringValue("$.booker.email").isEqualTo("test@example.com");
        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo("2024-12-01T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo("2024-12-02T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
    }

    @Test
    void testSerializeWithNullFields() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 12, 1, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 2, 10, 0, 0);

        BookingDto dto = BookingDto.builder()
                .id(1L)
                .item(null)
                .booker(null)
                .start(start)
                .end(end)
                .status(null)
                .build();

        JsonContent<BookingDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2024-12-01T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2024-12-02T10:00:00");
        assertThat(result).extractingJsonPathValue("$.item").isNull();
        assertThat(result).extractingJsonPathValue("$.booker").isNull();
        assertThat(result).extractingJsonPathValue("$.status").isNull();
    }

    @Test
    void testDeserialize() throws Exception {
        String jsonContent = String.join("",
                "{",
                "\"id\": 1,",
                "\"item\": {",
                "    \"id\": 10,",
                "    \"name\": \"Test Item\",",
                "    \"description\": \"Test Description\",",
                "    \"available\": true",
                "},",
                "\"booker\": {",
                "    \"id\": 100,",
                "    \"name\": \"Test User\",",
                "    \"email\": \"test@example.com\"",
                "},",
                "\"start\": \"2024-12-01T10:00:00\",",
                "\"end\": \"2024-12-02T10:00:00\",",
                "\"status\": \"WAITING\"",
                "}"
        );

        BookingDto dto = json.parse(jsonContent).getObject();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getItem()).isNotNull();
        assertThat(dto.getItem().getId()).isEqualTo(10L);
        assertThat(dto.getItem().getName()).isEqualTo("Test Item");
        assertThat(dto.getItem().getDescription()).isEqualTo("Test Description");
        assertThat(dto.getItem().getAvailable()).isTrue();
        assertThat(dto.getBooker()).isNotNull();
        assertThat(dto.getBooker().getId()).isEqualTo(100L);
        assertThat(dto.getBooker().getName()).isEqualTo("Test User");
        assertThat(dto.getBooker().getEmail()).isEqualTo("test@example.com");
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2024, 12, 1, 10, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2024, 12, 2, 10, 0, 0));
        assertThat(dto.getStatus()).isEqualTo("WAITING");
    }

    @Test
    void testDeserializeWithPartialData() throws Exception {
        String jsonContent = String.join("",
                "{",
                "\"id\": 1,",
                "\"start\": \"2024-12-01T10:00:00\",",
                "\"end\": \"2024-12-02T10:00:00\",",
                "\"status\": \"REJECTED\"",
                "}"
        );

        BookingDto dto = json.parse(jsonContent).getObject();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2024, 12, 1, 10, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2024, 12, 2, 10, 0, 0));
        assertThat(dto.getStatus()).isEqualTo("REJECTED");
        assertThat(dto.getItem()).isNull();
        assertThat(dto.getBooker()).isNull();
    }

    @Test
    void testDeserializeWithNullValues() throws Exception {
        String jsonContent = String.join("",
                "{",
                "\"id\": 1,",
                "\"item\": null,",
                "\"booker\": null,",
                "\"start\": \"2024-12-01T10:00:00\",",
                "\"end\": \"2024-12-02T10:00:00\",",
                "\"status\": null",
                "}"
        );

        BookingDto dto = json.parse(jsonContent).getObject();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getItem()).isNull();
        assertThat(dto.getBooker()).isNull();
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2024, 12, 1, 10, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2024, 12, 2, 10, 0, 0));
        assertThat(dto.getStatus()).isNull();
    }

    @Test
    void testEqualityAfterSerializationDeserialization() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 12, 1, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 2, 10, 0, 0);

        ItemDto item = ItemDto.builder()
                .id(10L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        UserDto booker = UserDto.builder()
                .id(100L)
                .name("Test User")
                .email("test@example.com")
                .build();

        BookingDto originalDto = BookingDto.builder()
                .id(1L)
                .item(item)
                .booker(booker)
                .start(start)
                .end(end)
                .status("WAITING")
                .build();

        String jsonString = this.json.write(originalDto).getJson();
        BookingDto deserializedDto = this.json.parseObject(jsonString);

        assertThat(deserializedDto.getId()).isEqualTo(originalDto.getId());
        assertThat(deserializedDto.getStart()).isEqualTo(originalDto.getStart());
        assertThat(deserializedDto.getEnd()).isEqualTo(originalDto.getEnd());
        assertThat(deserializedDto.getStatus()).isEqualTo(originalDto.getStatus());
        assertThat(deserializedDto.getItem().getId()).isEqualTo(originalDto.getItem().getId());
        assertThat(deserializedDto.getItem().getName()).isEqualTo(originalDto.getItem().getName());
        assertThat(deserializedDto.getBooker().getId()).isEqualTo(originalDto.getBooker().getId());
        assertThat(deserializedDto.getBooker().getName()).isEqualTo(originalDto.getBooker().getName());
    }
}