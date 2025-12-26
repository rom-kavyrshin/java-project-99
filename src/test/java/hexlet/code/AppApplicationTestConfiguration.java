package hexlet.code;

import net.datafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppApplicationTestConfiguration {

    @Bean
    public Faker getFaker() {
        return new Faker();
    }
}
