package ru.practicum.shareit.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

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
    void shouldReturnBadRequestStatusWhenHandleMethodArgumentNotValidWithSingleError() {
        String field = "email";
        String defaultMessage = "должно содержать @";

        FieldError fieldError = new FieldError("objectName", field, defaultMessage);
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ErrorResponse response = exceptionHandler.handleMethodArgumentNotValid(methodArgumentNotValidException);

        assertThat(response).isNotNull();
        assertThat(response.getError()).isEqualTo("email: должно содержать @");
    }

    @Test
    void shouldReturnBadRequestStatusWhenHandleMethodArgumentNotValidWithMultipleErrors() {
        FieldError fieldError1 = new FieldError(
                "objectName",
                "email",
                "должно содержать @"
        );
        FieldError fieldError2 = new FieldError(
                "objectName",
                "name",
                "не должно быть пустым"
        );

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ErrorResponse response = exceptionHandler.handleMethodArgumentNotValid(methodArgumentNotValidException);

        assertThat(response).isNotNull();
        assertThat(response.getError()).isEqualTo("email: должно содержать @; name: не должно быть пустым");
    }

    @Test
    void shouldReturnBadRequestStatusWhenHandleMethodArgumentNotValidWithNoErrors() {
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.emptyList());

        ErrorResponse response = exceptionHandler.handleMethodArgumentNotValid(methodArgumentNotValidException);

        assertThat(response).isNotNull();
        assertThat(response.getError()).isEmpty();
    }

    @Test
    void shouldReturnBadRequestStatusWhenHandleConstraintViolationWithSingleViolation() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);

        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(propertyPath.toString()).thenReturn("email");
        when(violation.getMessage()).thenReturn("должно содержать @");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation);

        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ErrorResponse response = exceptionHandler.handleConstraintViolation(exception);

        assertThat(response).isNotNull();
        assertThat(response.getError()).isEqualTo("email: должно содержать @");
    }

    @Test
    void shouldReturnBadRequestStatusWhenHandleConstraintViolationWithMultipleViolations() {
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        Path path1 = mock(Path.class);
        Path path2 = mock(Path.class);

        when(violation1.getPropertyPath()).thenReturn(path1);
        when(path1.toString()).thenReturn("email");
        when(violation1.getMessage()).thenReturn("должно содержать @");

        when(violation2.getPropertyPath()).thenReturn(path2);
        when(path2.toString()).thenReturn("name");
        when(violation2.getMessage()).thenReturn("не должно быть пустым");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation1);
        violations.add(violation2);

        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ErrorResponse response = exceptionHandler.handleConstraintViolation(exception);

        assertThat(response).isNotNull();
        String errorMessage = response.getError();
        assertThat(errorMessage).contains("email: должно содержать @");
        assertThat(errorMessage).contains("name: не должно быть пустым");
        assertThat(errorMessage).contains("; ");
    }

    @Test
    void shouldReturnBadRequestStatusWhenHandleConstraintViolationWithEmptyViolations() {
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ErrorResponse response = exceptionHandler.handleConstraintViolation(exception);

        assertThat(response).isNotNull();
        assertThat(response.getError()).isEmpty();
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
    void shouldReturnInternalServerErrorStatusWhenHandleMethodArgumentTypeMismatch() {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("id");
        when(exception.getRequiredType()).thenReturn((Class) Long.class);

        ErrorResponse response = exceptionHandler.handleMethodArgumentTypeMismatch(exception);

        assertThat(response).isNotNull();
        assertThat(response.getError()).isEqualTo("Неверный тип параметра 'id': ожидается Long");
    }

    @Test
    void shouldReturnInternalServerErrorStatusWhenHandleMethodArgumentTypeMismatchWithNullRequiredType() {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("id");
        when(exception.getRequiredType()).thenReturn(null);

        ErrorResponse response = exceptionHandler.handleMethodArgumentTypeMismatch(exception);

        assertThat(response).isNotNull();
        assertThat(response.getError()).isEqualTo("Неверный тип параметра 'id': ожидается неизвестный тип");
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