package hexlet.code.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.ModelGenerator;
import hexlet.code.app.dto.UserDTO;
import hexlet.code.app.model.User;
import hexlet.code.app.repositories.UserRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
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
    private Faker faker;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelGenerator modelGenerator;

    @BeforeEach
    public void setupMocks() {
        ArrayList<User> users = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            users.add(createMockUser());
        }

        System.out.println(users);

        userRepository.saveAll(users);
    }

    @Test
    public void testIndex() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(10)));
    }

    @Test
    public void testCreate() throws Exception {
        var userCreateDTO = Instancio.of(modelGenerator.getUserCreateDTOModel()).create();
        var userJson = objectMapper.writeValueAsString(userCreateDTO);

        var request = post("/api/users")
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
    public void testCreateWithInvalidEmail() throws Exception {
        var email = "example.com";
        var password = "somepassword";

        var map = new HashMap<String, String>();
        map.put("email", email);
        map.put("password", password);

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(map));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateWithInvalidPassword() throws Exception {
        var email = "kavyrshin@example.com";
        var password = "yo";

        var map = new HashMap<String, String>();
        map.put("email", email);
        map.put("password", password);

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(map));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdate() throws Exception {
        var userId = userRepository.findAll().getFirst().getId();
        var userForUpdate = userRepository.findAll().getFirst();

        var newUserData = Instancio.of(modelGenerator.getUserCreateDTOModel()).create();

        assertThat(userForUpdate.getFirstName(), not(newUserData.getFirstName()));
        assertThat(userForUpdate.getLastName(), not(newUserData.getLastName()));
        assertThat(userForUpdate.getEmail(), not(newUserData.getEmail()));

        var request = put("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUserData));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value(equalTo(newUserData.getEmail())))
                .andExpect(jsonPath("$.lastName").value(equalTo(newUserData.getEmail())))
                .andExpect(jsonPath("$.email").value(equalTo(newUserData.getEmail())));

        assertThat(userForUpdate.getFirstName(), equalTo(newUserData.getFirstName()));
        assertThat(userForUpdate.getLastName(), equalTo(newUserData.getLastName()));
        assertThat(userForUpdate.getEmail(), equalTo(newUserData.getEmail()));
    }

    private User createMockUser() {
        User user = new User();
        user.setFirstName(faker.name().firstName());
        user.setLastName(faker.name().lastName());
        user.setEmail(faker.internet().emailAddress());
        user.setPassword(faker.internet().password());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return user;
    }
}
