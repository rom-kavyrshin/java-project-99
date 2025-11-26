package hexlet.code.app.exception;

public class UnableDeleteException extends RuntimeException {
    public UnableDeleteException(String message) {
        super(message);
    }
}
