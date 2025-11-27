package hexlet.code.app.dto.label;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LabelDTO {

    private Long id;

    private String name;

    private LocalDateTime createdAt;
}
