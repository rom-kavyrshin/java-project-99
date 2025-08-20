package hexlet.code.app.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

@Component
public class CustomValidator {

    private final Validator validator;

    public CustomValidator(Validator validator) {
        this.validator = validator;
    }

    public <T> void validate(T object, Class<?>... groups) throws ConstraintViolationException {
        var violations = validator.validate(object, groups);

        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<T> constraintViolation : violations) {
                sb.append(constraintViolation.getMessage());
                sb.append("; ");
            }
            throw new ConstraintViolationException("Constraint violation: " + sb, violations);
        }
    }

}
