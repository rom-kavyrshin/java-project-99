package hexlet.code.app;

import net.datafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppApplicationConfigurationTest {

    @Bean
    public Faker getFaker() {
        return new Faker();
    }
}
