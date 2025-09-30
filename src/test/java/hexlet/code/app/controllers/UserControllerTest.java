package hexlet.code.app.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.ModelGenerator;
import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.dto.UserDTO;
import hexlet.code.app.dto.UserUpdateDTO;
import hexlet.code.app.repositories.UserRepository;
import hexlet.code.app.service.UserService;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private Faker faker;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static String token;

    private static final int USER_LIST_SIZE = 10;

    private UserCreateDTO testUser;
    private String testUserPassword;

    @BeforeEach
    void setupTest() throws Exception {
        setupMocks();
        setupToken();
    }

    void setupMocks() {
        userRepository.deleteAll();

        for (int i = 0; i < USER_LIST_SIZE; i++) {
            userService.create(Instancio.of(modelGenerator.getUserCreateDTOModel()).create());
        }

        testUser = Instancio.of(modelGenerator.getUserCreateDTOModel()).create();
        testUserPassword = testUser.getPassword();
        userService.create(testUser);
    }

    void setupToken() throws Exception {
        HashMap<String, String> loginData = new HashMap<>();
        loginData.put("username", testUser.getEmail());
        loginData.put("password", testUserPassword);

        var loginJson = objectMapper.writeValueAsString(loginData);

        var request = post("/api/login").contentType(MediaType.APPLICATION_JSON).content(loginJson);

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        token = "Bearer " + result;
    }

    @Test
    void testIndex() throws Exception {
        mockMvc.perform(get("/api/users").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(11)));
    }

    @Test
    void testShow() throws Exception {
        var userId = userRepository.findAll().getLast().getId();
        var user = userRepository.findById(userId).orElseThrow();

        mockMvc.perform(get("/api/users/" + userId).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(equalTo(user.getId()), Long.class))
                .andExpect(jsonPath("$.firstName").value(equalTo(user.getFirstName())))
                .andExpect(jsonPath("$.lastName").value(equalTo(user.getLastName())))
                .andExpect(jsonPath("$.email").value(equalTo(user.getEmail())))
                .andExpect(jsonPath("$.createdAt").value(equalTo(user.getCreatedAt().toString())))
                .andExpect(jsonPath("$.updatedAt").value(equalTo(user.getUpdatedAt().toString())));
    }

    @Test
    void testShowWithNonExistId() throws Exception {
        var userId = userRepository.findAll().getLast().getId();
        userRepository.deleteById(userId);

        mockMvc.perform(get("/api/users/" + userId).header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate() throws Exception {
        var userCreateDTO = Instancio.of(modelGenerator.getUserCreateDTOModel()).create();
        var userJson = objectMapper.writeValueAsString(userCreateDTO);

        var request = post("/api/users")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson);

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value(equalTo(userCreateDTO.getFirstName())))
                .andExpect(jsonPath("$.lastName").value(equalTo(userCreateDTO.getLastName())))
                .andExpect(jsonPath("$.email").value(equalTo(userCreateDTO.getEmail())))
                .andReturn();

        var resultUserDto = objectMapper.readValue(result.getResponse().getContentAsString(), UserDTO.class);
        var userFromRepository = userRepository.findById(resultUserDto.getId()).orElseThrow();

        assertThat(userCreateDTO.getFirstName(), equalTo(userFromRepository.getFirstName()));
        assertThat(userCreateDTO.getLastName(), equalTo(userFromRepository.getLastName()));
        assertThat(userCreateDTO.getEmail(), equalTo(userFromRepository.getEmail()));
    }

    @Test
    void testCreateWithInvalidEmail() throws Exception {
        var email = "example.com";
        var password = "somepassword";

        var map = new HashMap<String, String>();
        map.put("email", email);
        map.put("password", password);

        var request = post("/api/users")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(map));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateWithInvalidPassword() throws Exception {
        var email = "kavyrshin@example.com";
        var password = "yo";

        var map = new HashMap<String, String>();
        map.put("email", email);
        map.put("password", password);

        var request = post("/api/users")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(map));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdate() throws Exception {
        var userId = userRepository.findAll().getFirst().getId();
        var userForUpdate = userRepository.findById(userId).orElseThrow();

        var newUserData = Instancio.of(modelGenerator.getUserUpdateDTOModel()).create();

        assertThat(userForUpdate.getFirstName(), not(newUserData.getFirstName()));
        assertThat(userForUpdate.getLastName(), not(newUserData.getLastName()));
        assertThat(userForUpdate.getEmail(), not(newUserData.getEmail()));

        var request = put("/api/users/" + userId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUserData));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(equalTo(newUserData.getFirstName().get())))
                .andExpect(jsonPath("$.lastName").value(equalTo(newUserData.getLastName().get())))
                .andExpect(jsonPath("$.email").value(equalTo(newUserData.getEmail().get())));

        userForUpdate = userRepository.findById(userId).orElseThrow();

        assertThat(userForUpdate.getFirstName(), equalTo(newUserData.getFirstName().get()));
        assertThat(userForUpdate.getLastName(), equalTo(newUserData.getLastName().get()));
        assertThat(userForUpdate.getEmail(), equalTo(newUserData.getEmail().get()));
    }

    @Test
    void testPartlyUpdate() throws Exception {
        var userId = userRepository.findAll().getLast().getId();
        var userForUpdate = userRepository.findById(userId).orElseThrow();

        var newUserData = Instancio.of(modelGenerator.getUserUpdateDTOModel()).create();
        Map<String, Object> checkMap
                = objectMapper.convertValue(newUserData, new TypeReference<HashMap<String, Object>>() { });

        checkMap.remove("password");

        assertThat(userForUpdate.getFirstName(), not(newUserData.getFirstName()));
        assertThat(userForUpdate.getLastName(), not(newUserData.getLastName()));
        assertThat(userForUpdate.getEmail(), not(newUserData.getEmail()));

        var partNewUserData = new UserUpdateDTO();
        partNewUserData.setFirstName(newUserData.getFirstName());

        var request = put("/api/users/" + userId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partNewUserData));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(equalTo(newUserData.getFirstName().get())))
                .andExpect(jsonPath("$.lastName").value(equalTo(userForUpdate.getLastName())))
                .andExpect(jsonPath("$.email").value(equalTo(userForUpdate.getEmail())));

        userForUpdate = userRepository.findById(userId).orElseThrow();

        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!! " + objectMapper.writeValueAsString(userForUpdate));

        assertThat(userForUpdate.getFirstName(), equalTo(newUserData.getFirstName().get()));

        checkMap.remove("firstName");
        /*--------------------------------*/

        partNewUserData = new UserUpdateDTO();
        partNewUserData.setLastName(newUserData.getLastName());

        request = put("/api/users/" + userId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partNewUserData));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(equalTo(userForUpdate.getFirstName())))
                .andExpect(jsonPath("$.lastName").value(equalTo(newUserData.getLastName().get())))
                .andExpect(jsonPath("$.email").value(equalTo(userForUpdate.getEmail())));

        userForUpdate = userRepository.findById(userId).orElseThrow();

        assertThat(userForUpdate.getLastName(), equalTo(newUserData.getLastName().get()));

        checkMap.remove("lastName");
        /*--------------------------------*/

        partNewUserData = new UserUpdateDTO();
        partNewUserData.setEmail(newUserData.getEmail());

        request = put("/api/users/" + userId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partNewUserData));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(equalTo(userForUpdate.getFirstName())))
                .andExpect(jsonPath("$.lastName").value(equalTo(userForUpdate.getLastName())))
                .andExpect(jsonPath("$.email").value(equalTo(newUserData.getEmail().get())));

        userForUpdate = userRepository.findById(userId).orElseThrow();

        assertThat(userForUpdate.getLastName(), equalTo(newUserData.getLastName().get()));

        checkMap.remove("email");

        /*--------------------------------*/

        partNewUserData = new UserUpdateDTO();
        partNewUserData.setLastName(JsonNullable.of(null));

        request = put("/api/users/" + userId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partNewUserData));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(equalTo(userForUpdate.getFirstName())))
                .andExpect(jsonPath("$.lastName").doesNotExist())
                .andExpect(jsonPath("$.email").value(equalTo(userForUpdate.getEmail())));

        userForUpdate = userRepository.findById(userId).orElseThrow();

        assertThat(userForUpdate.getLastName(), equalTo(null));

        /*--------------------------------*/
        assertTrue(checkMap.isEmpty());
    }

    @Test
    void testUpdatePassword() throws Exception {
        var userId = userRepository.findAll().getLast().getId();
        var userForUpdate = userRepository.findById(userId).orElseThrow();

        var newPassword = faker.internet().password();

        assertFalse(passwordEncoder.matches(newPassword, userForUpdate.getPassword()));

        var partNewUserData = new UserUpdateDTO();
        partNewUserData.setPassword(JsonNullable.of(newPassword));

        var request = put("/api/users/" + userId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partNewUserData));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        userForUpdate = userRepository.findById(userId).orElseThrow();

        assertTrue(passwordEncoder.matches(newPassword, userForUpdate.getPassword()));
    }

    @Test
    void testUpdateToNullRequiredFields() throws Exception {
        var userId = userRepository.findAll().getLast().getId();
        var userForUpdate = userRepository.findById(userId).orElseThrow();

        String newEmail = null;

        assertThat(userForUpdate.getEmail(), not(newEmail));

        var partNewUserData = new UserUpdateDTO();
        partNewUserData.setEmail(JsonNullable.of(newEmail));

        var request = put("/api/users/" + userId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partNewUserData));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        userForUpdate = userRepository.findById(userId).orElseThrow();

        assertThat(userForUpdate.getEmail(), not(newEmail));
    }

    @Test
    void testUpdateToInvalidEmail() throws Exception {
        var userId = userRepository.findAll().getLast().getId();
        var userForUpdate = userRepository.findById(userId).orElseThrow();

        String newEmail = "rkexample.com";

        assertThat(userForUpdate.getEmail(), not(newEmail));

        var partNewUserData = new UserUpdateDTO();
        partNewUserData.setEmail(JsonNullable.of(newEmail));

        var request = put("/api/users/" + userId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partNewUserData));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        userForUpdate = userRepository.findById(userId).orElseThrow();

        assertThat(userForUpdate.getEmail(), not(newEmail));
    }

    @Test
    void testDelete() throws Exception {
        var userId = userRepository.findAll().getLast().getId();

        assertTrue(userRepository.findById(userId).isPresent());

        mockMvc.perform(get("/api/users/" + userId).header("Authorization", token))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/users/" + userId).header("Authorization", token))
                .andExpect(status().isNoContent());

        assertTrue(userRepository.findById(userId).isEmpty());
    }
}
