package hexlet.code.app.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.ModelGenerator;
import hexlet.code.app.dto.label.LabelDTO;
import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task.TaskDTO;
import hexlet.code.app.dto.task.TaskParamsDTO;
import hexlet.code.app.dto.task.TaskUpdateDTO;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.model.User;
import hexlet.code.app.repositories.TaskRepository;
import hexlet.code.app.repositories.UserRepository;
import hexlet.code.app.service.LabelsService;
import hexlet.code.app.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static hexlet.code.app.util.HeaderUtils.X_TOTAL_COUNT_HEADER_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {

    private static final String DEFAULT_USERNAME = "hexlet@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskService taskService;

    @Autowired
    private LabelsService labelService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private Faker faker;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelGenerator modelGenerator;

    @Value("${default-user.password}")
    private String defaultUserPassword;

    private static String token;

    private static final int USER_LIST_SIZE = 4;
    private static final int TASK_LIST_SIZE = 20;
    private static final int LABEL_LIST_SIZE = 20;

    private TaskDTO testTask;
    private final ArrayList<User> usersList = new ArrayList<>();
    private final ArrayList<LabelDTO> labelsList = new ArrayList<>();

    @BeforeEach
    void setupTest() throws Exception {
        setupMocks();
        setupToken();
    }

    @AfterEach
    void cleanup() {
        taskRepository.deleteAll();
        userRepository.deleteAll(usersList);

        for (LabelDTO label : labelsList) {
            labelService.delete(label.getId());
        }
    }

    void setupMocks() {
        for (int i = 0; i < USER_LIST_SIZE; i++) {
            var user = Instancio.of(modelGenerator.getUserCreateDTOModel()).create();
            usersList.add(userMapper.map(user));
        }

        userRepository.saveAll(usersList);

        for (int i = 0; i < LABEL_LIST_SIZE; i++) {
            var label = Instancio.of(modelGenerator.getLabelCreateDTOModel()).create();
            var labelDto = labelService.create(label);
            labelsList.add(labelDto);
        }

        for (int i = 0; i < TASK_LIST_SIZE; i++) {
            var task = Instancio.of(modelGenerator.getTaskCreateDTOModel()).create();
            taskService.create(task);
        }

        var testTaskDTO = Instancio.of(modelGenerator.getTaskCreateDTOModel()).create();
        testTask = taskService.create(testTaskDTO);
    }

    void setupToken() throws Exception {
        HashMap<String, String> loginData = new HashMap<>();
        loginData.put("username", DEFAULT_USERNAME);
        loginData.put("password", defaultUserPassword);

        var loginJson = objectMapper.writeValueAsString(loginData);

        var request = post("/api/login").contentType(MediaType.APPLICATION_JSON).content(loginJson);

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        token = "Bearer " + result;
    }

    @Test
    void testIndex() throws Exception {
        mockMvc.perform(get("/api/tasks").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(header().string(X_TOTAL_COUNT_HEADER_NAME, "21"))
                .andExpect(jsonPath("$").value(hasSize(21)));
    }

    private Long newTask(
            Long index,
            String title,
            String content,
            String status,
            Long assigneeId,
            List<Long> taskLabelIds
    ) {
        return taskService.create(
                new TaskCreateDTO(
                        index,
                        title,
                        content,
                        status,
                        assigneeId,
                        taskLabelIds
                )
        ).getId();
    }

    @Test
    void testIndexWithFilter() throws Exception {
        taskRepository.deleteAll();

        var firstUser = usersList.get(1);
        var secondUser = usersList.get(2);

        var firstLabel = labelsList.get(0).getId();
        var secondLabel = labelsList.get(1).getId();
        var thirdLabel = labelsList.get(2).getId();
        var fourthLabel = labelsList.get(3).getId();

        var firstId = newTask(
                1L, "Ремонт стола", "Отремонтировать стол. Заменит столешницу",
                "to_be_fixed", secondUser.getId(), List.of(fourthLabel)
        );
        var secondId = newTask(
                2L, "Прочистить раковину", "Вроде и так все понятно",
                "to_publish", firstUser.getId(), List.of(thirdLabel, fourthLabel)
        );
        var thirdId = newTask(
                null, "Почистить стиральную машину", "Профилактика",
                "draft", firstUser.getId(), List.of()
        );
        var fourthId = newTask(
                null, "Позавтракать", "Плотненький приём пищи",
                "to_publish", firstUser.getId(), List.of(firstLabel, secondLabel)
        );
        var fifthId = newTask(
                null, "Заказать еду", "Ну понятно",
                "to_publish", secondUser.getId(), List.of(secondLabel, thirdLabel)
        );
        var sixthId = newTask(
                null, "Купить одежду", "Освежить свой outfit",
                "to_publish", firstUser.getId(), List.of(thirdLabel)
        );
        var seventhId = newTask(
                null, "Дела", "Дела дела дела",
                "published", firstUser.getId(), List.of(firstLabel, fourthLabel)
        );
        var eighthId = newTask(
                null, "Обед", "Покушать",
                "published", null, List.of(firstLabel)
        );
        var ninthId = newTask(
                null, "Отдых", "Небольшой отдых на кроватке",
                "published", firstUser.getId(), List.of(fourthLabel)
        );
        var tenthId = newTask(
                null, "Снова дела", "Возвращаюсь к делам",
                "to_review", firstUser.getId(), List.of(secondLabel)
        );
        var eleventhId = newTask(
                null, "Продать старый стол", "Есть другой стол. Это не нужен и мешает",
                "draft", secondUser.getId(), List.of(secondLabel, fourthLabel)
        );

        mockMvc.perform(
                get("/api/tasks")
                        .queryParam("titleCont", "чист")
                        .header("Authorization", token)
                )
                .andExpect(status().isOk())
                .andExpect(header().string(X_TOTAL_COUNT_HEADER_NAME, "2"))
                .andExpect(jsonPath("$").value(hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(secondId))
                .andExpect(jsonPath("$[1].id").value(thirdId));

        mockMvc.perform(
                        get("/api/tasks")
                                .queryParam("assigneeId", secondUser.getId() + "")
                                .header("Authorization", token)
                )
                .andExpect(status().isOk())
                .andExpect(header().string(X_TOTAL_COUNT_HEADER_NAME, "3"))
                .andExpect(jsonPath("$").value(hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(firstId))
                .andExpect(jsonPath("$[1].id").value(fifthId))
                .andExpect(jsonPath("$[2].id").value(eleventhId));

        mockMvc.perform(
                        get("/api/tasks")
                                .queryParam("status", "published")
                                .header("Authorization", token)
                )
                .andExpect(status().isOk())
                .andExpect(header().string(X_TOTAL_COUNT_HEADER_NAME, "3"))
                .andExpect(jsonPath("$").value(hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(seventhId))
                .andExpect(jsonPath("$[1].id").value(eighthId))
                .andExpect(jsonPath("$[2].id").value(ninthId));

        mockMvc.perform(
                        get("/api/tasks")
                                .queryParam("labelId", secondLabel + "")
                                .header("Authorization", token)
                )
                .andExpect(status().isOk())
                .andExpect(header().string(X_TOTAL_COUNT_HEADER_NAME, "4"))
                .andExpect(jsonPath("$").value(hasSize(4)))
                .andExpect(jsonPath("$[0].id").value(fourthId))
                .andExpect(jsonPath("$[1].id").value(fifthId))
                .andExpect(jsonPath("$[2].id").value(tenthId))
                .andExpect(jsonPath("$[3].id").value(eleventhId));

        ///////////////

        mockMvc.perform(
                        get("/api/tasks")
                                .queryParam("titleCont", "од")
                                .queryParam("assigneeId", firstUser.getId() + "")
                                .queryParam("status", "to_publish")
                                .queryParam("labelId", thirdLabel + "")
                                .header("Authorization", token)
                )
                .andExpect(status().isOk())
                .andExpect(header().string(X_TOTAL_COUNT_HEADER_NAME, "1"))
                .andExpect(jsonPath("$").value(hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(sixthId));
    }

    @Test
    void testShow() throws Exception {
        var taskForShow = testTask;

        var result = mockMvc.perform(get("/api/tasks/" + testTask.getId()).header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        var resultTaskDto
                = objectMapper.readValue(result.getResponse().getContentAsString(), TaskDTO.class);

        assertEquals(taskForShow.getId(), resultTaskDto.getId());
        assertEquals(taskForShow.getIndex(), resultTaskDto.getIndex());
        assertEquals(taskForShow.getTitle(), resultTaskDto.getTitle());
        assertEquals(taskForShow.getContent(), resultTaskDto.getContent());
        assertEquals(taskForShow.getStatus(), resultTaskDto.getStatus());
        assertEquals(taskForShow.getAssigneeId(), resultTaskDto.getAssigneeId());
        assertTrue(Duration.between(taskForShow.getCreatedAt(), resultTaskDto.getCreatedAt()).abs().toMillis() < 1);
    }

    @Test
    void testShowWithNonExistId() throws Exception {
        var list = taskService.getAll(new TaskParamsDTO());
        var taskId = list.get(list.size() / 2).getId();

        taskService.delete(taskId);

        mockMvc.perform(get("/api/tasks/" + taskId).header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate() throws Exception {
        var taskCreateDTO = Instancio.of(modelGenerator.getTaskCreateDTOModel()).create();
        var taskJson = objectMapper.writeValueAsString(taskCreateDTO);

        var request = post("/api/tasks")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJson);

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var resultTaskDto
                = objectMapper.readValue(result.getResponse().getContentAsString(), TaskDTO.class);

        assertEquals(taskCreateDTO.getIndex(), resultTaskDto.getIndex());
        assertEquals(taskCreateDTO.getTitle(), resultTaskDto.getTitle());
        assertEquals(taskCreateDTO.getContent(), resultTaskDto.getContent());
        assertEquals(taskCreateDTO.getStatus(), resultTaskDto.getStatus());
        assertEquals(taskCreateDTO.getAssigneeId(), resultTaskDto.getAssigneeId());

        var taskFromRepository = taskService.getById(resultTaskDto.getId());

        assertEquals(taskCreateDTO.getIndex(), taskFromRepository.getIndex());
        assertEquals(taskCreateDTO.getTitle(), taskFromRepository.getTitle());
        assertEquals(taskCreateDTO.getContent(), taskFromRepository.getContent());
        assertEquals(taskCreateDTO.getStatus(), taskFromRepository.getStatus());
        assertEquals(taskCreateDTO.getAssigneeId(), taskFromRepository.getAssigneeId());
    }

    @Test
    void testCreateWithInvalidTitle() throws Exception {
        var title = "";
        var content = "some content";
        var status = "draft";

        var map = new HashMap<String, String>();
        map.put("title", title);
        map.put("content", content);
        map.put("status", status);

        var request = post("/api/tasks")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(map));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateWithNullStatus() throws Exception {
        var title = "Постирать одежду";
        var content = "some content";

        var map = new HashMap<String, String>();
        map.put("title", title);
        map.put("content", content);

        var request = post("/api/tasks")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(map));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateWithInvalidStatus() throws Exception {
        var title = "Постирать одежду";
        var content = "some content";
        var status = "drift";

        var map = new HashMap<String, String>();
        map.put("title", title);
        map.put("content", content);
        map.put("status", status);

        var request = post("/api/tasks")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(map));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateWithInvalidAssignee() throws Exception {
        var title = "Постирать одежду";
        var content = "some content";
        var status = "draft";
        var assigneeId = 100500;

        var map = new HashMap<String, Object>();
        map.put("title", title);
        map.put("content", content);
        map.put("status", status);
        map.put("assignee_id", assigneeId);

        var request = post("/api/tasks")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(map));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateWithoutAssignee() throws Exception {
        var title = "Постирать одежду";
        var content = "some content";
        var status = "draft";
        Long assigneeId = null;

        var map = new HashMap<String, Object>();
        map.put("title", title);
        map.put("content", content);
        map.put("status", status);
        map.put("assignee_id", assigneeId);

        var request = post("/api/tasks")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(map));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var resultTaskDto
                = objectMapper.readValue(result.getResponse().getContentAsString(), TaskDTO.class);

        assertEquals(title, resultTaskDto.getTitle());
        assertEquals(content, resultTaskDto.getContent());
        assertEquals(status, resultTaskDto.getStatus());
        assertNull(resultTaskDto.getAssigneeId());

        var taskFromRepository = taskService.getById(resultTaskDto.getId());

        assertEquals(title, taskFromRepository.getTitle());
        assertEquals(content, taskFromRepository.getContent());
        assertEquals(status, taskFromRepository.getStatus());
        assertNull(taskFromRepository.getAssigneeId());
    }

    @Test
    void testCreateWithInvalidLabelId() throws Exception {
        var title = "Постирать одежду";
        var content = "some content";
        var status = "draft";
        var wrongLabelId1 = 100500;
        var wrongLabelId2 = 1500;

        var map = new HashMap<String, Object>();
        map.put("title", title);
        map.put("content", content);
        map.put("status", status);
        map.put("taskLabelIds", new int[]{1, 2, wrongLabelId1, wrongLabelId2});

        var request = post("/api/tasks")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(map));

        var result = mockMvc.perform(request).andReturn();
        var resultContent = result.getResponse().getContentAsString();


        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        assertFalse(resultContent.contains(1 + ","));
        assertFalse(resultContent.contains(2 + ","));
        assertTrue(resultContent.contains(wrongLabelId1 + ""));
        assertTrue(resultContent.contains(wrongLabelId2 + ""));
    }

    @Test
    void testUpdate() throws Exception {
        var taskForUpdate = testTask;
        var taskId = testTask.getId();

        var newTaskData = modelGenerator.getFullyDifferentTask(taskForUpdate);

        assertThat(taskForUpdate.getIndex(), not(newTaskData.getIndex()));
        assertThat(taskForUpdate.getTitle(), not(newTaskData.getTitle()));
        assertThat(taskForUpdate.getContent(), not(newTaskData.getContent()));
        assertThat(taskForUpdate.getStatus(), not(newTaskData.getStatus()));
        assertThat(taskForUpdate.getAssigneeId(), not(newTaskData.getAssigneeId()));
        assertThat(taskForUpdate.getTaskLabelIds(), not(newTaskData.getTaskLabelIds()));

        var request = put("/api/tasks/" + taskId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTaskData));

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var resultTaskDto
                = objectMapper.readValue(result.getResponse().getContentAsString(), TaskDTO.class);

        assertEquals(newTaskData.getIndex().get(), resultTaskDto.getIndex());
        assertEquals(newTaskData.getTitle().get(), resultTaskDto.getTitle());
        assertEquals(newTaskData.getContent().get(), resultTaskDto.getContent());
        assertEquals(newTaskData.getStatus().get(), resultTaskDto.getStatus());
        assertEquals(newTaskData.getAssigneeId().get(), resultTaskDto.getAssigneeId());
        assertEquals(newTaskData.getTaskLabelIds().get(), resultTaskDto.getTaskLabelIds());

        taskForUpdate = taskService.getById(taskId);

        assertEquals(newTaskData.getIndex().get(), taskForUpdate.getIndex());
        assertEquals(newTaskData.getTitle().get(), taskForUpdate.getTitle());
        assertEquals(newTaskData.getContent().get(), taskForUpdate.getContent());
        assertEquals(newTaskData.getStatus().get(), taskForUpdate.getStatus());
        assertEquals(newTaskData.getAssigneeId().get(), taskForUpdate.getAssigneeId());
        assertEquals(newTaskData.getTaskLabelIds().get(), taskForUpdate.getTaskLabelIds());
    }

    @Test
    void testPartlyUpdate() throws Exception {
        var taskForUpdate = testTask;
        var taskId = taskForUpdate.getId();

        var newTitle = "Updated";
        String newContent = null;

        assertNotEquals(newTitle, taskForUpdate.getTitle());
        assertNotEquals(newContent, taskForUpdate.getContent());

        var partNewTaskData = new TaskUpdateDTO();
        partNewTaskData.setTitle(JsonNullable.of(newTitle));
        partNewTaskData.setContent(JsonNullable.of(newContent));

        var request = put("/api/tasks/" + taskId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partNewTaskData));

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var resultTaskDto
                = objectMapper.readValue(result.getResponse().getContentAsString(), TaskDTO.class);

        assertEquals(taskForUpdate.getId(), resultTaskDto.getId());
        assertEquals(taskForUpdate.getIndex(), resultTaskDto.getIndex());
        assertEquals(newTitle, resultTaskDto.getTitle());
        assertEquals(newContent, resultTaskDto.getContent());
        assertEquals(taskForUpdate.getStatus(), resultTaskDto.getStatus());
        assertEquals(taskForUpdate.getAssigneeId(), resultTaskDto.getAssigneeId());
        assertTrue(Duration.between(taskForUpdate.getCreatedAt(), resultTaskDto.getCreatedAt()).abs().toMillis() < 1);

        var updatedTask = taskService.getById(taskId);

        assertEquals(taskForUpdate.getId(), updatedTask.getId());
        assertEquals(taskForUpdate.getIndex(), updatedTask.getIndex());
        assertEquals(newTitle, updatedTask.getTitle());
        assertEquals(newContent, updatedTask.getContent());
        assertEquals(taskForUpdate.getStatus(), updatedTask.getStatus());
        assertEquals(taskForUpdate.getAssigneeId(), updatedTask.getAssigneeId());
        assertTrue(Duration.between(taskForUpdate.getCreatedAt(), updatedTask.getCreatedAt()).abs().toMillis() < 1);
    }

    @Test
    void testUpdateDTOJsonNullable() {
        var taskUpdateDTO = Instancio.of(modelGenerator.getTaskUpdateDTOModel()).lenient().create();
        Map<String, Object> checkMap
                = objectMapper.convertValue(taskUpdateDTO, new TypeReference<HashMap<String, Object>>() { });

        assertEquals(JsonNullable.class, taskUpdateDTO.getIndex().getClass());
        checkMap.remove("index");

        assertEquals(JsonNullable.class, taskUpdateDTO.getTitle().getClass());
        checkMap.remove("title");

        assertEquals(JsonNullable.class, taskUpdateDTO.getContent().getClass());
        checkMap.remove("content");

        assertEquals(JsonNullable.class, taskUpdateDTO.getStatus().getClass());
        checkMap.remove("status");

        assertEquals(JsonNullable.class, taskUpdateDTO.getAssigneeId().getClass());
        checkMap.remove("assignee_id");

        assertEquals(JsonNullable.class, taskUpdateDTO.getTaskLabelIds().getClass());
        checkMap.remove("taskLabelIds");

        assertTrue(checkMap.isEmpty());
    }

    @Test
    void testUpdateToNullRequiredFields() throws Exception {
        var taskForUpdate = testTask;
        var taskId = taskForUpdate.getId();

        String newTitle = null;

        assertNotEquals(newTitle, taskForUpdate.getTitle());

        var partNewTaskData = new TaskUpdateDTO();
        partNewTaskData.setTitle(JsonNullable.of(newTitle));

        var request = put("/api/tasks/" + taskId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partNewTaskData));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        taskForUpdate = taskService.getById(taskId);

        assertNotEquals(newTitle, taskForUpdate.getTitle());
    }

    @Test
    void testUpdateToInvalidTitle() throws Exception {
        var taskForUpdate = testTask;
        var taskId = taskForUpdate.getId();

        String newTitle = "";

        assertNotEquals(newTitle, taskForUpdate.getTitle());

        var partNewTaskData = new TaskUpdateDTO();
        partNewTaskData.setTitle(JsonNullable.of(newTitle));

        var request = put("/api/tasks/" + taskId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partNewTaskData));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        taskForUpdate = taskService.getById(taskId);

        assertNotEquals(newTitle, taskForUpdate.getTitle());
    }

    @Test
    void testDelete() throws Exception {
        var taskForUpdate = testTask;
        var taskId = taskForUpdate.getId();

        assertTrue(taskRepository.findById(taskId).isPresent());

        mockMvc.perform(get("/api/tasks/" + taskId).header("Authorization", token))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/tasks/" + taskId).header("Authorization", token))
                .andExpect(status().isNoContent());

        assertTrue(taskRepository.findById(taskId).isEmpty());
    }
}
