package hexlet.code.app.component;

import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.repositories.UserRepository;
import hexlet.code.app.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    public static final String DEFAULT_USER_EMAIL = "hexlet@example.com";

    private final UserService userService;
    private final UserRepository userRepository;

    @Value("${default-user.password}")
    private String defaultUserPassword;

    public DataInitializer(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByEmail(DEFAULT_USER_EMAIL)) {
            var user = new UserCreateDTO();
            user.setEmail(DEFAULT_USER_EMAIL);
            user.setPassword(defaultUserPassword);
            userService.create(user);
        }
    }
}
