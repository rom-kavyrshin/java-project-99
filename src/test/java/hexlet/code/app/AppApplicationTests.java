package hexlet.code.app;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AppApplicationTests {

    @Autowired
    TestRestTemplate testRestTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void testWelcome() {
        String response = testRestTemplate.getForObject("/welcome", String.class);
        Assertions.assertEquals("Welcome to Spring", response);
    }

}
