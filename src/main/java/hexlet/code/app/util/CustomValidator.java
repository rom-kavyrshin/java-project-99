package hexlet.code.app.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomValidator {

    @Autowired
    private Validator validator;

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
