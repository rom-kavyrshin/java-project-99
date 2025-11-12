package hexlet.code.app.exception;

public class DependentResourceNotFoundException extends RuntimeException {
    public DependentResourceNotFoundException(String message) {
        super(message);
    }
}
