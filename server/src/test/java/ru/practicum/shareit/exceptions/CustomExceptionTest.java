package ru.practicum.shareit.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomExceptionTest {

    @Test
    void shouldCreateWithMessageAccessException() {
        String message = "Доступ запрещен";

        AccessException exception = new AccessException(message);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldBeSubclassOfRuntimeExceptionAccessException() {
        String message = "Тестовое сообщение";

        AccessException exception = new AccessException(message);

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }


    @Test
    void shouldCreateWithMessageNotFoundException() {
        String message = "Object not found";

        NotFoundException exception = new NotFoundException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void shouldCreateWithMessageValidationException() {
        String message = "Validation failed";

        ValidationException exception = new ValidationException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void shouldCreateWithMessageConflictException() {
        String message = "Conflict detected";

        ConflictException exception = new ConflictException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void shouldCreateWithMessageForErrorResponse() {
        String errorMessage = "Test error";

        ErrorResponse errorResponse = new ErrorResponse(errorMessage);

        assertThat(errorResponse.getError()).isEqualTo(errorMessage);
    }

    @Test
    void shouldHaveNoArgsConstructorForSerializationForErrorResponse() {
        ErrorResponse errorResponse = new ErrorResponse();

        assertThat(errorResponse).isNotNull();
    }

    @Test
    void shouldHaveGettersAndSettersForErrorResponse() {
        String errorMessage = "Test error";
        ErrorResponse errorResponse = new ErrorResponse();

        errorResponse.setError(errorMessage);

        assertThat(errorResponse.getError()).isEqualTo(errorMessage);
    }
}