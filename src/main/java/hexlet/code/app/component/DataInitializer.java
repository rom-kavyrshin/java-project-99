package hexlet.code.app.component;

import hexlet.code.app.dto.label.LabelCreateDTO;
import hexlet.code.app.dto.task_status.TaskStatusCreateDTO;
import hexlet.code.app.dto.user.UserCreateDTO;
import hexlet.code.app.mapper.LabelMapper;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.repositories.LabelRepository;
import hexlet.code.app.repositories.TaskStatusRepository;
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

    private final TaskStatusRepository taskStatusRepository;
    private final TaskStatusMapper taskStatusMapper;

    private final LabelRepository labelRepository;
    private final LabelMapper labelMapper;

    @Value("${default-user.password}")
    private String defaultUserPassword;

    public DataInitializer(
            UserRepository userRepository,
            UserMapper userMapper,
            TaskStatusRepository taskStatusRepository,
            TaskStatusMapper taskStatusMapper,
            LabelRepository labelRepository,
            LabelMapper labelMapper
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.taskStatusRepository = taskStatusRepository;
        this.taskStatusMapper = taskStatusMapper;
        this.labelRepository = labelRepository;
        this.labelMapper = labelMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        createDefaultUser(args);
        createDefaultTaskStatuses(args);
        createDefaultLabels(args);
    }

    public void createDefaultUser(ApplicationArguments args) {
        if (!userRepository.existsByEmail(DEFAULT_USER_EMAIL)) {
            var user = new UserCreateDTO();
            user.setEmail(DEFAULT_USER_EMAIL);
            user.setPassword(defaultUserPassword);
            userRepository.save(userMapper.map(user));
        }
    }

    public void createDefaultTaskStatuses(ApplicationArguments args) {
        if (taskStatusRepository.count() == 0) {
            var draft = new TaskStatusCreateDTO("Draft", "draft");
            var toReview = new TaskStatusCreateDTO("To Review", "to_review");
            var toBeFixed = new TaskStatusCreateDTO("To Be Fixed", "to_be_fixed");
            var toPublish = new TaskStatusCreateDTO("To Publish", "to_publish");
            var published = new TaskStatusCreateDTO("Published", "published");

            taskStatusRepository.save(taskStatusMapper.map(draft));
            taskStatusRepository.save(taskStatusMapper.map(toReview));
            taskStatusRepository.save(taskStatusMapper.map(toBeFixed));
            taskStatusRepository.save(taskStatusMapper.map(toPublish));
            taskStatusRepository.save(taskStatusMapper.map(published));
        }
    }

    public void createDefaultLabels(ApplicationArguments args) {
        if (labelRepository.count() == 0) {
            var feature = new LabelCreateDTO("feature");
            var bug = new LabelCreateDTO("bug");

            labelRepository.save(labelMapper.map(feature));
            labelRepository.save(labelMapper.map(bug));
        }
    }
}
