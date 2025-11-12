package hexlet.code.app;

import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task.TaskDTO;
import hexlet.code.app.dto.task.TaskUpdateDTO;
import hexlet.code.app.dto.task_status.TaskStatusCreateDTO;
import hexlet.code.app.dto.task_status.TaskStatusUpdateDTO;
import hexlet.code.app.dto.user.UserCreateDTO;
import hexlet.code.app.dto.user.UserUpdateDTO;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.repositories.TaskStatusRepository;
import hexlet.code.app.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Getter
@Component
public class ModelGenerator {

    private Model<UserCreateDTO> userCreateDTOModel;
    private Model<UserUpdateDTO> userUpdateDTOModel;
    private Model<User> userModel;

    private Model<TaskStatusCreateDTO> taskStatusCreateDTOModel;
    private Model<TaskStatusUpdateDTO> taskStatusUpdateDTOModel;
    private Model<TaskStatus> taskStatusModel;

    private Model<TaskCreateDTO> taskCreateDTOModel;
    private Model<TaskUpdateDTO> taskUpdateDTOModel;

    @Autowired
    private Faker faker;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @PostConstruct
    private void init() {
        initUserModels();
        initTaskStatusModels();
        initTaskModels();
    }

    private void initUserModels() {
        userCreateDTOModel = Instancio.of(UserCreateDTO.class)
                .supply(Select.field(UserCreateDTO::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(UserCreateDTO::getLastName), () -> faker.name().lastName())
                .supply(Select.field(UserCreateDTO::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(UserCreateDTO::getPassword), () -> faker.internet().password())
                .toModel();

        userUpdateDTOModel = Instancio.of(UserUpdateDTO.class)
                .supply(Select.field(UserUpdateDTO::getFirstName), () -> JsonNullable.of(faker.name().firstName()))
                .supply(Select.field(UserUpdateDTO::getLastName), () -> JsonNullable.of(faker.name().lastName()))
                .supply(Select.field(UserUpdateDTO::getEmail), () -> JsonNullable.of(faker.internet().emailAddress()))
                .supply(Select.field(UserUpdateDTO::getPassword), () -> JsonNullable.of(faker.internet().password()))
                .toModel();

        userModel = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .ignore(Select.field(User::getCreatedAt))
                .ignore(Select.field(User::getUpdatedAt))
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getPasswordDigest), () -> faker.internet().password())
                .toModel();
    }

    private void initTaskStatusModels() {
        taskStatusCreateDTOModel = Instancio.of(TaskStatusCreateDTO.class)
                .supply(Select.field(TaskStatusCreateDTO::getName), () -> faker.internet().slug())
                .supply(Select.field(TaskStatusCreateDTO::getSlug), () -> faker.internet().slug())
                .toModel();

        taskStatusUpdateDTOModel = Instancio.of(TaskStatusUpdateDTO.class)
                .supply(Select.field(TaskStatusUpdateDTO::getName), () -> JsonNullable.of(faker.internet().slug()))
                .supply(Select.field(TaskStatusUpdateDTO::getSlug), () -> JsonNullable.of(faker.internet().slug()))
                .toModel();

        taskStatusModel = Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .ignore(Select.field(TaskStatus::getCreatedAt))
                .supply(Select.field(TaskStatus::getName), () -> faker.internet().slug())
                .supply(Select.field(TaskStatus::getSlug), () -> faker.internet().slug())
                .toModel();
    }

    private void initTaskModels() {
        taskCreateDTOModel = Instancio.of(TaskCreateDTO.class)
                .supply(Select.field(TaskCreateDTO::getIndex), () -> (long) faker.number().numberBetween(1000, 9999))
                .supply(Select.field(TaskCreateDTO::getTitle), () -> faker.departed().character())
                .supply(Select.field(TaskCreateDTO::getContent), () -> faker.departed().quote())
                .supply(Select.field(TaskCreateDTO::getStatus), () -> getStatusSlugFromRepository())
                .supply(Select.field(TaskCreateDTO::getAssigneeId), () -> getUserIdFromRepositoryOrNull())
                .toModel();

        taskUpdateDTOModel = Instancio.of(TaskUpdateDTO.class)
                .supply(Select.field(TaskCreateDTO::getIndex), () -> JsonNullable.of(faker.number().numberBetween(1000, 9999)))
                .supply(Select.field(TaskCreateDTO::getTitle), () -> JsonNullable.of(faker.departed().character()))
                .supply(Select.field(TaskCreateDTO::getContent), () -> JsonNullable.of(faker.departed().quote()))
                .supply(Select.field(TaskCreateDTO::getStatus), () -> JsonNullable.of(getStatusSlugFromRepository()))
                .supply(Select.field(TaskCreateDTO::getAssigneeId), () -> JsonNullable.of(getUserIdFromRepositoryOrNull()))
                .toModel();
    }

    public TaskUpdateDTO getFullyDifferentTask(TaskDTO original) {
        var result = new TaskUpdateDTO();
        result.setIndex(JsonNullable.of(original.getIndex() + 11));
        result.setTitle(JsonNullable.of(original.getTitle() + " updated"));
        result.setContent(JsonNullable.of(original.getContent() + " updated"));
        result.setStatus(JsonNullable.of(getStatusSlugFromRepository(original.getStatus())));
        result.setAssigneeId(JsonNullable.of(getUserIdFromRepositoryOrNull(original.getAssigneeId())));

        return result;
    }

    private Long getUserIdFromRepositoryOrNull() {
        return getUserIdFromRepositoryOrNull(null);
    }

    private Long getUserIdFromRepositoryOrNull(Long filter) {
        var random = new SecureRandom();

        if (random.nextBoolean()) {
            var users = userRepository.findAll().stream().filter(it -> !it.getId().equals(filter)).toList();
            return users.get(random.nextInt(users.size())).getId();
        }

        return null;
    }

    private String getStatusSlugFromRepository() {
        return getStatusSlugFromRepository(null);
    }

    private String getStatusSlugFromRepository(String filter) {
        var random = new SecureRandom();

        var taskStatuses = taskStatusRepository.findAll().stream().filter(it -> !it.getSlug().equals(filter)).toList();
        return taskStatuses.get(random.nextInt(taskStatuses.size())).getSlug();
    }
}
