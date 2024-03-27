package cinema.error;


// we can write like this case
//@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class AlreadyBookException extends RuntimeException {
    public AlreadyBookException(String message) {
        super(message);
    }
}
