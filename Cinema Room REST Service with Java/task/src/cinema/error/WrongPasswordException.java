package cinema.error;

public class WrongPasswordException extends RuntimeException {
    public WrongPasswordException(String message) {
        super(message);
    }
}
