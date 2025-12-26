package hexlet.code.exception;

public class DependentResourceNotFoundException extends RuntimeException {
    public DependentResourceNotFoundException(String message) {
        super(message);
    }
}
