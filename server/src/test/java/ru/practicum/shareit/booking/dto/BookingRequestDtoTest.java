package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingRequestDtoTest {

    @Autowired
    private JacksonTester<BookingRequestDto> json;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now().withNano(0);
    }

    @Test
    void testSerialize() throws Exception {
        LocalDateTime start = now.plusDays(1).withNano(0);
        LocalDateTime end = now.plusDays(2).withNano(0);

        BookingRequestDto dto = BookingRequestDto.builder()
                .id(1L)
                .itemId(10L)
                .booker(100L)
                .start(start)
                .end(end)
                .build();

        JsonContent<BookingRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(10);
        assertThat(result).extractingJsonPathNumberValue("$.booker").isEqualTo(100);
        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo(start.toString());
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo(end.toString());
    }

    @Test
    void testDeserialize() throws Exception {
        String jsonContent = String.join("",
                "{",
                "\"id\": 1,",
                "\"itemId\": 10,",
                "\"booker\": 100,",
                "\"start\": \"2024-12-01T10:00:00\",",
                "\"end\": \"2024-12-02T10:00:00\"",
                "}"
        );

        BookingRequestDto dto = json.parse(jsonContent).getObject();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getItemId()).isEqualTo(10L);
        assertThat(dto.getBooker()).isEqualTo(100L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2024, 12, 1, 10, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2024, 12, 2, 10, 0, 0));
    }

    @Test
    void testDeserializeWithPartialData() throws Exception {
        String jsonContent = String.join("",
                "{",
                "\"itemId\": 10,",
                "\"start\": \"2024-12-01T10:00:00\",",
                "\"end\": \"2024-12-02T10:00:00\"",
                "}"
        );

        BookingRequestDto dto = json.parse(jsonContent).getObject();

        assertThat(dto.getItemId()).isEqualTo(10L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2024, 12, 1, 10, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2024, 12, 2, 10, 0, 0));
        assertThat(dto.getId()).isNull();
        assertThat(dto.getBooker()).isNull();
    }
}