package hexlet.code.app;

import hexlet.code.app.dto.task_status.TaskStatusCreateDTO;
import hexlet.code.app.dto.task_status.TaskStatusUpdateDTO;
import hexlet.code.app.dto.user.UserCreateDTO;
import hexlet.code.app.dto.user.UserUpdateDTO;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ModelGenerator {

    private Model<UserCreateDTO> userCreateDTOModel;
    private Model<UserUpdateDTO> userUpdateDTOModel;
    private Model<User> userModel;

    private Model<TaskStatusCreateDTO> taskStatusCreateDTOModel;
    private Model<TaskStatusUpdateDTO> taskStatusUpdateDTOModel;
    private Model<TaskStatus> taskStatusModel;

    @Autowired
    private Faker faker;

    @PostConstruct
    private void init() {
        initUserModels();
        initTaskStatusModels();
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
}
