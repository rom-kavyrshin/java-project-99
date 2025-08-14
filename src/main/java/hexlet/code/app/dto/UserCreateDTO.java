package hexlet.code.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateDTO {
    private String firstName;

    private String lastName;

    @NotNull
    @Email
    private String email;

    @NotNull
    @Size(min = 3)
    private String password;
}
