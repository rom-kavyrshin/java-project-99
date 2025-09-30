package hexlet.code.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.ModelGenerator;
import hexlet.code.app.dto.UserCreateDTO;
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

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerSecurityTest {

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
    private static final int MIDDLE_OF_THE_LIST = USER_LIST_SIZE / 2;

    private UserCreateDTO testUser;
    private String testUserPassword;

    @BeforeEach
    void setupTest() throws Exception {
        setupMocks();
        setupToken();
    }

    void setupMocks() {
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
    void testUpdateStrangerUserData() throws Exception {
        var userId = userRepository.findAll().get(MIDDLE_OF_THE_LIST).getId();
        var userForUpdate = userRepository.findById(userId).orElseThrow();

        var newUserData = new UserUpdateDTO();
        newUserData.setFirstName(JsonNullable.of("Updated"));
        newUserData.setLastName(JsonNullable.of("Updated"));
        newUserData.setEmail(JsonNullable.of("updated@example.com"));

        assertNotEquals(newUserData.getFirstName().get(), userForUpdate.getFirstName());
        assertNotEquals(newUserData.getLastName().get(), userForUpdate.getLastName());
        assertNotEquals(newUserData.getEmail().get(), userForUpdate.getEmail());

        var request = put("/api/users/" + userId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUserData));

        mockMvc.perform(request)
                .andExpect(status().isForbidden());

        userForUpdate = userRepository.findById(userId).orElseThrow();

        assertNotEquals(newUserData.getFirstName().get(), userForUpdate.getFirstName());
        assertNotEquals(newUserData.getLastName().get(), userForUpdate.getLastName());
        assertNotEquals(newUserData.getEmail().get(), userForUpdate.getEmail());
    }

}
