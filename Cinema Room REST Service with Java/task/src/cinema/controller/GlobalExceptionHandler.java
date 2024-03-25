package cinema.controller;

import cinema.error.AlreadyBookException;
import cinema.model.APIErrorResponse;
import cinema.error.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<APIErrorResponse> handleNotFoundException(NotFoundException ex) {
        APIErrorResponse response = new APIErrorResponse(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AlreadyBookException.class)
    public ResponseEntity<APIErrorResponse> alreadyBookException(AlreadyBookException ex) {
        APIErrorResponse response = new APIErrorResponse(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ArrayIndexOutOfBoundsException.class)
    public ResponseEntity<APIErrorResponse> arrayIndexOutofBoundsException(ArrayIndexOutOfBoundsException ex) {
        APIErrorResponse response = new APIErrorResponse(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
