package hexlet.code.app.component;

import hexlet.code.app.dto.user.UserCreateDTO;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    public static final String DEFAULT_USER_EMAIL = "hexlet@example.com";

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Value("${default-user.password}")
    private String defaultUserPassword;

    public DataInitializer(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByEmail(DEFAULT_USER_EMAIL)) {
            var user = new UserCreateDTO();
            user.setEmail(DEFAULT_USER_EMAIL);
            user.setPassword(defaultUserPassword);
            userRepository.save(userMapper.map(user));
        }
    }
}
