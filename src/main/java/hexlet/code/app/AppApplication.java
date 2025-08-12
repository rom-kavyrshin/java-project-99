package hexlet.code.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AppApplication {

    private static final Logger LOG = LoggerFactory.getLogger(AppApplication.class);

    @Value("${spring.profiles.active}")
    private String activeProfile;

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        LOG.info("Active profile {}", activeProfile);
    }

}
