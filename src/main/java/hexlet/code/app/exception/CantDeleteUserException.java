package hexlet.code.app.exception;

public class CantDeleteUserException extends RuntimeException {
    public CantDeleteUserException(String message) {
        super(message);
    }
}
