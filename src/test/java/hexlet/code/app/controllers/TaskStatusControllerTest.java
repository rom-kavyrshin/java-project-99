package hexlet.code.app.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.ModelGenerator;
import hexlet.code.app.dto.task_status.TaskStatusCreateDTO;
import hexlet.code.app.dto.task_status.TaskStatusDTO;
import hexlet.code.app.dto.task_status.TaskStatusUpdateDTO;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.repositories.TaskStatusRepository;
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

import java.util.HashMap;
import java.util.Map;

import static hexlet.code.app.util.HeaderUtils.X_TOTAL_COUNT_HEADER_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
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
public class TaskStatusControllerTest {

    private static final String DEFAULT_USERNAME = "hexlet@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskStatusMapper taskStatusMapper;

    @Autowired
    private Faker faker;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelGenerator modelGenerator;

    @Value("${default-user.password}")
    private String defaultUserPassword;

    private static String token;

    private static final int TASK_STATUS_LIST_SIZE = 5;

    private TaskStatusCreateDTO testTaskStatus;

    @BeforeEach
    void setupTest() throws Exception {
        setupMocks();
        setupToken();
    }

    void setupMocks() {
        taskStatusRepository.deleteAll();

        for (int i = 0; i < TASK_STATUS_LIST_SIZE; i++) {
            var taskStatus = Instancio.of(modelGenerator.getTaskStatusCreateDTOModel()).create();
            taskStatusRepository.save(taskStatusMapper.map(taskStatus));
        }

        testTaskStatus = Instancio.of(modelGenerator.getTaskStatusCreateDTOModel()).create();
        taskStatusRepository.save(taskStatusMapper.map(testTaskStatus));
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
        mockMvc.perform(get("/api/task_statuses").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(header().string(X_TOTAL_COUNT_HEADER_NAME, "6"))
                .andExpect(jsonPath("$").value(hasSize(6)));
    }

    @Test
    void testShow() throws Exception {
        var taskStatus = taskStatusRepository.findBySlug(testTaskStatus.getSlug()).orElseThrow();

        var result = mockMvc.perform(get("/api/task_statuses/" + taskStatus.getId()).header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        var resultTaskStatusDto
                = objectMapper.readValue(result.getResponse().getContentAsString(), TaskStatusDTO.class);

        assertEquals(taskStatus.getId(), resultTaskStatusDto.getId());
        assertEquals(taskStatus.getName(), resultTaskStatusDto.getName());
        assertEquals(taskStatus.getSlug(), resultTaskStatusDto.getSlug());
        assertEquals(taskStatus.getCreatedAt(), resultTaskStatusDto.getCreatedAt());
    }

    @Test
    void testShowWithNonExistId() throws Exception {
        var list = taskStatusRepository.findAll();
        var taskStatusId = list.get(list.size() / 2).getId();

        taskStatusRepository.deleteById(taskStatusId);

        mockMvc.perform(get("/api/task_statuses/" + taskStatusId).header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate() throws Exception {
        var taskStatusCreateDTO = Instancio.of(modelGenerator.getTaskStatusCreateDTOModel()).create();
        var taskStatusJson = objectMapper.writeValueAsString(taskStatusCreateDTO);

        var request = post("/api/task_statuses")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskStatusJson);

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var resultTaskStatusDto
                = objectMapper.readValue(result.getResponse().getContentAsString(), TaskStatusDTO.class);

        assertThat(taskStatusCreateDTO.getName(), equalTo(resultTaskStatusDto.getName()));
        assertThat(taskStatusCreateDTO.getSlug(), equalTo(resultTaskStatusDto.getSlug()));

        var taskStatusFromRepository = taskStatusRepository.findById(resultTaskStatusDto.getId()).orElseThrow();

        assertThat(taskStatusCreateDTO.getName(), equalTo(taskStatusFromRepository.getName()));
        assertThat(taskStatusCreateDTO.getSlug(), equalTo(taskStatusFromRepository.getSlug()));
    }

    @Test
    void testCreateWithInvalidName() throws Exception {
        var name = "";
        var slug = "s";

        var map = new HashMap<String, String>();
        map.put("name", name);
        map.put("slug", slug);

        var request = post("/api/task_statuses")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(map));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateWithInvalidPassword() throws Exception {
        var name = "s";
        var slug = "";

        var map = new HashMap<String, String>();
        map.put("name", name);
        map.put("slug", slug);

        var request = post("/api/task_statuses")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(map));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdate() throws Exception {
        var taskStatusForUpdate = taskStatusRepository.findBySlug(testTaskStatus.getSlug()).orElseThrow();
        var taskStatusId = taskStatusForUpdate.getId();

        var newTaskStatusData = Instancio.of(modelGenerator.getTaskStatusUpdateDTOModel()).create();

        assertThat(taskStatusForUpdate.getName(), not(newTaskStatusData.getName()));
        assertThat(taskStatusForUpdate.getSlug(), not(newTaskStatusData.getSlug()));

        var request = put("/api/task_statuses/" + taskStatusId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTaskStatusData));

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var resultTaskStatusDto
                = objectMapper.readValue(result.getResponse().getContentAsString(), TaskStatusDTO.class);

        assertEquals(newTaskStatusData.getName().get(), resultTaskStatusDto.getName());
        assertEquals(newTaskStatusData.getSlug().get(), resultTaskStatusDto.getSlug());

        taskStatusForUpdate = taskStatusRepository.findById(taskStatusId).orElseThrow();

        assertEquals(newTaskStatusData.getName().get(), taskStatusForUpdate.getName());
        assertEquals(newTaskStatusData.getSlug().get(), taskStatusForUpdate.getSlug());
    }

    @Test
    void testPartlyUpdate() throws Exception {
        var taskStatusForUpdate = taskStatusRepository.findBySlug(testTaskStatus.getSlug()).orElseThrow();
        var taskStatusId = taskStatusForUpdate.getId();

        var newTaskStatusData = Instancio.of(modelGenerator.getTaskStatusUpdateDTOModel()).create();
        Map<String, Object> checkMap
                = objectMapper.convertValue(newTaskStatusData, new TypeReference<HashMap<String, Object>>() { });

        assertNotEquals(newTaskStatusData.getName().get(), taskStatusForUpdate.getName());
        assertNotEquals(newTaskStatusData.getSlug().get(), taskStatusForUpdate.getSlug());

        var partNewTaskStatusData = new TaskStatusUpdateDTO();
        partNewTaskStatusData.setName(newTaskStatusData.getName());

        var request = put("/api/task_statuses/" + taskStatusId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partNewTaskStatusData));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(equalTo(newTaskStatusData.getName().get())))
                .andExpect(jsonPath("$.slug").value(equalTo(taskStatusForUpdate.getSlug())));

        taskStatusForUpdate = taskStatusRepository.findById(taskStatusId).orElseThrow();

        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!! " + objectMapper.writeValueAsString(taskStatusForUpdate));

        assertEquals(newTaskStatusData.getName().get(), taskStatusForUpdate.getName());

        checkMap.remove("name");
        /*--------------------------------*/

        partNewTaskStatusData = new TaskStatusUpdateDTO();
        partNewTaskStatusData.setSlug(newTaskStatusData.getSlug());

        request = put("/api/task_statuses/" + taskStatusId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partNewTaskStatusData));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(equalTo(taskStatusForUpdate.getName())))
                .andExpect(jsonPath("$.slug").value(equalTo(newTaskStatusData.getSlug().get())));

        taskStatusForUpdate = taskStatusRepository.findById(taskStatusId).orElseThrow();

        assertEquals(newTaskStatusData.getSlug().get(), taskStatusForUpdate.getSlug());

        checkMap.remove("slug");

        /*--------------------------------*/
        assertTrue(checkMap.isEmpty());
    }

    @Test
    void testUpdateToNullRequiredFields() throws Exception {
        var taskStatusForUpdate = taskStatusRepository.findBySlug(testTaskStatus.getSlug()).orElseThrow();
        var taskStatusId = taskStatusForUpdate.getId();

        String newSlug = null;

        assertNotEquals(newSlug, taskStatusForUpdate.getSlug());

        var partNewTaskStatusData = new TaskStatusUpdateDTO();
        partNewTaskStatusData.setSlug(JsonNullable.of(newSlug));

        var request = put("/api/task_statuses/" + taskStatusId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partNewTaskStatusData));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        taskStatusForUpdate = taskStatusRepository.findBySlug(testTaskStatus.getSlug()).orElseThrow();

        assertNotEquals(newSlug, taskStatusForUpdate.getSlug());
    }

    @Test
    void testUpdateToInvalidSlug() throws Exception {
        var taskStatusForUpdate = taskStatusRepository.findBySlug(testTaskStatus.getSlug()).orElseThrow();
        var taskStatusId = taskStatusForUpdate.getId();

        String newSlug = "";

        assertNotEquals(newSlug, taskStatusForUpdate.getSlug());

        var partNewTaskStatusData = new TaskStatusUpdateDTO();
        partNewTaskStatusData.setSlug(JsonNullable.of(newSlug));

        var request = put("/api/task_statuses/" + taskStatusId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partNewTaskStatusData));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        taskStatusForUpdate = taskStatusRepository.findBySlug(testTaskStatus.getSlug()).orElseThrow();

        assertNotEquals(newSlug, taskStatusForUpdate.getSlug());
    }

    @Test
    void testDelete() throws Exception {
        var taskStatusForDelete = taskStatusRepository.findBySlug(testTaskStatus.getSlug()).orElseThrow();
        var taskStatusId = taskStatusForDelete.getId();

        assertTrue(taskStatusRepository.findById(taskStatusId).isPresent());

        mockMvc.perform(get("/api/task_statuses/" + taskStatusId).header("Authorization", token))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/task_statuses/" + taskStatusId).header("Authorization", token))
                .andExpect(status().isNoContent());

        assertTrue(taskStatusRepository.findById(taskStatusId).isEmpty());
    }
}
