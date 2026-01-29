package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ShareItServerTest {

    @Test
    void contextLoads() {
        assertTrue(true);
    }

    @Test
    void mainMethodRunsSuccessfully() {
        assertDoesNotThrow(() -> ShareItServer.main(new String[]{}));
    }
}