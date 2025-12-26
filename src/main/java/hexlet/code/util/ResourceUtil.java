package hexlet.code.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class ResourceUtil {

    public String readResourceFileAsString(String privateKeyPath) throws IOException {
        var resource = new ClassPathResource(privateKeyPath);
        try (var inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

}
