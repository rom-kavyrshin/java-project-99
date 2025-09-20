package hexlet.code.app;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AppApplicationTests {

    @Autowired
    TestRestTemplate testRestTemplate;

    private static String token;

    @BeforeEach
    void setupToken() {
        if (token == null) {
            String loginJson = """
                    {
                    "username": "hexlet@example.com",
                    "password": "a123"
                    }
                    """;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(loginJson, headers);

            ResponseEntity<String> response = testRestTemplate.exchange(
                    "/api/login",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            String result = response.getBody();
            token = "Bearer " + result;
        }

        testRestTemplate.getRestTemplate().getInterceptors().add(
                (request, body, execution) -> {
                    request.getHeaders().add("Authorization", token);
                    return execution.execute(request, body);
                });
    }

    @Test
    void contextLoads() {
    }

    @Test
    void testWelcome() {
        String response = testRestTemplate.getForObject("/welcome", String.class);
        Assertions.assertEquals("Welcome to Spring", response);
    }

}
