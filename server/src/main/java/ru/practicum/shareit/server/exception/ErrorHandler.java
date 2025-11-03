package ru.practicum.shareit.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleNotFoundException(final NotFoundException ex) {
        return new ErrorMessage(ex.getMessage());
    }

    @ExceptionHandler(EmailConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorMessage handleEmailConflictException(final EmailConflictException ex) {
        return new ErrorMessage(ex.getMessage());
    }

    @ExceptionHandler(NoAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorMessage handleNoAccessException(final NoAccessException ex) {
        return new ErrorMessage(ex.getMessage());
    }

    @ExceptionHandler(BookingDateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleBookingDateException(final BookingDateException ex) {
        return new ErrorMessage(ex.getMessage());
    }

    @ExceptionHandler(NotAvailableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleNotAvailableException(final NotAvailableException ex) {
        return new ErrorMessage(ex.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handleUnpredictedException(final Throwable ex) {
        return new ErrorMessage("Произошла непредвиденная ошибка (" + ex.getClass().getName() + "): " + ex.getMessage());
    }
}
