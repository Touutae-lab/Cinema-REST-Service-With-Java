package cinema.error;

public class AlreadyBookException extends RuntimeException {
    public AlreadyBookException(String message) {
        super(message);
    }
}
