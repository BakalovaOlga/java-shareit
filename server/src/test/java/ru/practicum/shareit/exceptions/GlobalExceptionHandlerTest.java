package ru.practicum.shareit.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;


    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void shouldReturnNotFoundStatusWhenHandleNotFoundException() {
        String errorMessage = "Пользователь с id=1 не найден";
        NotFoundException exception = new NotFoundException(errorMessage);

        ErrorResponse response = exceptionHandler.handleNotFoundException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getError()).isEqualTo(errorMessage);
    }

    @Test
    void shouldReturnBadRequestStatusWhenHandleValidationException() {
        String errorMessage = "Некорректные данные запроса";

        ValidationException exception = new ValidationException(errorMessage);

        ErrorResponse response = exceptionHandler.handleValidationException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getError()).isEqualTo(errorMessage);
    }

    @Test
    void shouldReturnConflictStatusWhenHandleConflictException() {
        String errorMessage = "Пользователь с таким email уже существует";
        ConflictException exception = new ConflictException(errorMessage);

        ErrorResponse response = exceptionHandler.handleConflictException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getError()).isEqualTo(errorMessage);
    }

    @Test
    void shouldReturnBadRequestStatusWhenHandleIllegalArgumentException() {
        String errorMessage = "Некорректный аргумент метода";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        ErrorResponse response = exceptionHandler.handleIllegalArgumentException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getError()).isEqualTo(errorMessage);
    }

    @Test
    void shouldReturnInternalServerErrorStatusWhenHandleOtherExceptions() {
        String errorMessage = "Неожиденная ошибка";
        Exception exception = new RuntimeException(errorMessage);

        ErrorResponse response = exceptionHandler.handleOtherExceptions(exception);

        assertThat(response).isNotNull();
        assertThat(response.getError()).isEqualTo(errorMessage);
    }

    @Test
    void shouldReturnProperErrorResponseWhenHandleException() {
        String errorMessage = "Test error message";
        Exception exception = new Exception(errorMessage);

        ErrorResponse response = exceptionHandler.handleOtherExceptions(exception);

        assertThat(response).isNotNull();
        assertThat(response.getError()).isEqualTo(errorMessage);
    }

    @Test
    void shouldHaveCorrectStructureWhenErrorResponse() {
        String errorMessage = "Test error";
        ErrorResponse errorResponse = new ErrorResponse(errorMessage);

        assertThat(errorResponse).hasFieldOrPropertyWithValue("error", errorMessage);
        assertThat(errorResponse.getError()).isEqualTo(errorMessage);
    }
}