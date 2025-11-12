package hexlet.code.app.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.ModelGenerator;
import hexlet.code.app.dto.task.TaskDTO;
import hexlet.code.app.dto.task.TaskUpdateDTO;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.model.Task;
import hexlet.code.app.repositories.TaskRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static hexlet.code.app.util.HeaderUtils.X_TOTAL_COUNT_HEADER_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {

    private static final String DEFAULT_USERNAME = "hexlet@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private Faker faker;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelGenerator modelGenerator;

    @Value("${default-user.password}")
    private String defaultUserPassword;

    private static String token;

    private static final int TASK_LIST_SIZE = 20;

    private Task testTask;

    @BeforeEach
    void setupTest() throws Exception {
        setupMocks();
        setupToken();
    }

    void setupMocks() {
        taskRepository.deleteAll();

        for (int i = 0; i < TASK_LIST_SIZE; i++) {
            var task = Instancio.of(modelGenerator.getTaskCreateDTOModel()).create();
            taskRepository.save(taskMapper.map(task));
        }

        var testTaskDTO = Instancio.of(modelGenerator.getTaskCreateDTOModel()).create();
        testTask = taskRepository.save(taskMapper.map(testTaskDTO));
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

    @Test
    void testShow() throws Exception {
        var taskForShow = taskMapper.map(testTask);

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
        var list = taskRepository.findAll();
        var taskId = list.get(list.size() / 2).getId();

        taskRepository.deleteById(taskId);

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

        var taskFromRepository = taskMapper.map(taskRepository.findById(resultTaskDto.getId()).orElseThrow());

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
    void testUpdate() throws Exception {
        var taskForUpdate = taskMapper.map(testTask);
        var taskId = testTask.getId();

        var newTaskData = modelGenerator.getFullyDifferentTask(taskForUpdate);

        assertThat(taskForUpdate.getIndex(), not(newTaskData.getIndex()));
        assertThat(taskForUpdate.getTitle(), not(newTaskData.getTitle()));
        assertThat(taskForUpdate.getContent(), not(newTaskData.getContent()));
        assertThat(taskForUpdate.getStatus(), not(newTaskData.getStatus()));
        assertThat(taskForUpdate.getAssigneeId(), not(newTaskData.getAssigneeId()));

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

        taskForUpdate = taskMapper.map(taskRepository.findById(taskId).orElseThrow());

        assertEquals(newTaskData.getIndex().get(), taskForUpdate.getIndex());
        assertEquals(newTaskData.getTitle().get(), taskForUpdate.getTitle());
        assertEquals(newTaskData.getContent().get(), taskForUpdate.getContent());
        assertEquals(newTaskData.getStatus().get(), taskForUpdate.getStatus());
        assertEquals(newTaskData.getAssigneeId().get(), taskForUpdate.getAssigneeId());
    }

    @Test
    void testPartlyUpdate() throws Exception {
        var taskForUpdate = taskMapper.map(testTask);
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

        var updatedTask = taskMapper.map(taskRepository.findById(taskId).orElseThrow());

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

        assertTrue(checkMap.isEmpty());
    }

    @Test
    void testUpdateToNullRequiredFields() throws Exception {
        var taskForUpdate = taskMapper.map(testTask);
        var taskId = taskForUpdate.getId();

        String newTitle = null;

        assertNotEquals(newTitle, taskForUpdate.getTitle());

        var partNewTaskStatusData = new TaskUpdateDTO();
        partNewTaskStatusData.setTitle(JsonNullable.of(newTitle));

        var request = put("/api/tasks/" + taskId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partNewTaskStatusData));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        taskForUpdate = taskMapper.map(taskRepository.findById(taskId).orElseThrow());

        assertNotEquals(newTitle, taskForUpdate.getTitle());
    }

    @Test
    void testUpdateToInvalidTitle() throws Exception {
        var taskForUpdate = taskMapper.map(testTask);
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

        taskForUpdate = taskMapper.map(taskRepository.findById(taskId).orElseThrow());

        assertNotEquals(newTitle, taskForUpdate.getTitle());
    }

    @Test
    void testDelete() throws Exception {
        var taskForUpdate = taskMapper.map(testTask);
        var taskId = taskForUpdate.getId();

        assertTrue(taskRepository.findById(taskId).isPresent());

        mockMvc.perform(get("/api/tasks/" + taskId).header("Authorization", token))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/tasks/" + taskId).header("Authorization", token))
                .andExpect(status().isNoContent());

        assertTrue(taskRepository.findById(taskId).isEmpty());
    }
}
