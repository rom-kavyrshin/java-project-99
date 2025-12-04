package hexlet.code.app.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.ModelGenerator;
import hexlet.code.app.dto.label.LabelDTO;
import hexlet.code.app.dto.label.LabelUpdateDTO;
import hexlet.code.app.mapper.LabelMapper;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.model.Label;
import hexlet.code.app.model.User;
import hexlet.code.app.repositories.LabelRepository;
import hexlet.code.app.repositories.UserRepository;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static hexlet.code.app.util.HeaderUtils.X_TOTAL_COUNT_HEADER_NAME;
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
public class LabelsControllerTest {

    private static final String DEFAULT_USERNAME = "hexlet@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelMapper labelMapper;

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
    private static final int LABELS_LIST_SIZE = 20;

    private Label testLabel;
    private final ArrayList<User> usersList = new ArrayList<>();
    private final ArrayList<Label> labelsList = new ArrayList<>();

    @BeforeEach
    void setupTest() throws Exception {
        setupMocks();
        setupToken();
    }

    @AfterEach
    void cleanup() {
        labelRepository.deleteAll(labelsList);
        userRepository.deleteAll(usersList);
    }

    void setupMocks() {
        for (int i = 0; i < USER_LIST_SIZE; i++) {
            var user = Instancio.of(modelGenerator.getUserCreateDTOModel()).create();
            usersList.add(userMapper.map(user));
        }

        userRepository.saveAll(usersList);

        for (int i = 0; i < LABELS_LIST_SIZE; i++) {
            var label = Instancio.of(modelGenerator.getLabelCreateDTOModel()).create();
            labelsList.add(labelMapper.map(label));
        }

        var testLabelDTO = Instancio.of(modelGenerator.getLabelCreateDTOModel()).create();
        testLabel = labelRepository.save(labelMapper.map(testLabelDTO));

        labelRepository.saveAll(labelsList);
        labelsList.add(testLabel);
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
        mockMvc.perform(get("/api/labels").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(header().string(X_TOTAL_COUNT_HEADER_NAME, "23"))
                .andExpect(jsonPath("$").value(hasSize(23)));
    }

    @Test
    void testShow() throws Exception {
        var labelForShow = labelMapper.map(testLabel);

        var result = mockMvc.perform(get("/api/labels/" + testLabel.getId()).header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();

        var resultLabelDto
                = objectMapper.readValue(result.getResponse().getContentAsString(), LabelDTO.class);

        assertEquals(labelForShow.getId(), resultLabelDto.getId());
        assertEquals(labelForShow.getName(), resultLabelDto.getName());
        assertTrue(Duration.between(labelForShow.getCreatedAt(), resultLabelDto.getCreatedAt()).abs().toMillis() < 1);
    }

    @Test
    void testShowWithNonExistId() throws Exception {
        var list = labelRepository.findAll();
        var labelId = list.get(list.size() / 2).getId();

        labelRepository.deleteById(labelId);

        mockMvc.perform(get("/api/labels/" + labelId).header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate() throws Exception {
        var labelCreateDTO = Instancio.of(modelGenerator.getLabelCreateDTOModel()).create();
        var labelJson = objectMapper.writeValueAsString(labelCreateDTO);

        var request = post("/api/labels")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(labelJson);

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var resultLabelDto
                = objectMapper.readValue(result.getResponse().getContentAsString(), LabelDTO.class);

        assertEquals(labelCreateDTO.getName(), resultLabelDto.getName());

        var taskFromRepository = labelMapper.map(labelRepository.findById(resultLabelDto.getId()).orElseThrow());

        assertEquals(labelCreateDTO.getName(), taskFromRepository.getName());
    }

    @Test
    void testCreateWithInvalidName() throws Exception {
        var name = "ab";

        var map = new HashMap<String, String>();
        map.put("name", name);

        var request = post("/api/labels")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(map));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateWithNullName() throws Exception {
        String name = null;

        var map = new HashMap<String, String>();
        map.put("name", name);

        var request = post("/api/labels")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(map));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdate() throws Exception {
        var labelForUpdate = labelMapper.map(testLabel);
        var labelId = testLabel.getId();

        var newLabelData = Instancio.of(modelGenerator.getLabelUpdateDTOModel()).create();

        assertNotEquals(labelForUpdate.getName(), newLabelData.getName().get());

        var request = put("/api/labels/" + labelId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newLabelData));

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var resultLabelDto
                = objectMapper.readValue(result.getResponse().getContentAsString(), LabelDTO.class);

        assertEquals(newLabelData.getName().get(), resultLabelDto.getName());

        labelForUpdate = labelMapper.map(labelRepository.findById(labelId).orElseThrow());

        assertEquals(newLabelData.getName().get(), labelForUpdate.getName());
    }

    @Test
    void testUpdateDTOJsonNullable() {
        var labelUpdateDTO = Instancio.of(modelGenerator.getLabelUpdateDTOModel()).lenient().create();
        Map<String, Object> checkMap
                = objectMapper.convertValue(labelUpdateDTO, new TypeReference<HashMap<String, Object>>() { });

        assertEquals(JsonNullable.class, labelUpdateDTO.getName().getClass());
        checkMap.remove("name");

        assertTrue(checkMap.isEmpty());
    }

    @Test
    void testUpdateToNullRequiredFields() throws Exception {
        var labelForUpdate = labelMapper.map(testLabel);
        var labelId = labelForUpdate.getId();

        String newName = null;

        assertNotEquals(newName, labelForUpdate.getName());

        var partNewLabelData = new LabelUpdateDTO();
        partNewLabelData.setName(JsonNullable.of(newName));

        var request = put("/api/labels/" + labelId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partNewLabelData));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        labelForUpdate = labelMapper.map(labelRepository.findById(labelId).orElseThrow());

        assertNotEquals(newName, labelForUpdate.getName());
    }

    @Test
    void testUpdateToInvalidName() throws Exception {
        var labelForUpdate = labelMapper.map(testLabel);
        var labelId = labelForUpdate.getId();

        String newName = "cd";

        assertNotEquals(newName, labelForUpdate.getName());

        var partNewLabelData = new LabelUpdateDTO();
        partNewLabelData.setName(JsonNullable.of(newName));

        var request = put("/api/labels/" + labelId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partNewLabelData));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        labelForUpdate = labelMapper.map(labelRepository.findById(labelId).orElseThrow());

        assertNotEquals(newName, labelForUpdate.getName());
    }

    @Test
    void testDelete() throws Exception {
        var labelForUpdate = labelMapper.map(testLabel);
        var labelId = labelForUpdate.getId();

        assertTrue(labelRepository.findById(labelId).isPresent());

        mockMvc.perform(get("/api/labels/" + labelId).header("Authorization", token))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/labels/" + labelId).header("Authorization", token))
                .andExpect(status().isNoContent());

        assertTrue(labelRepository.findById(labelId).isEmpty());
    }

    @Test
    void testGetByName() {
        var label = labelMapper.map(testLabel);
        var labelName = label.getName();

        assertTrue(labelRepository.findByName(labelName).isPresent());
        assertEquals(label.getId(), labelRepository.findByName(labelName).orElseThrow().getId());
    }
}
