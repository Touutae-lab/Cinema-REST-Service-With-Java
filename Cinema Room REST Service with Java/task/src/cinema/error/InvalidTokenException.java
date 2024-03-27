package cinema.error;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
}