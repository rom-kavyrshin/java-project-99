package hexlet.code.app.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TaskCreateDTO {

    private Long index;

    @NotNull
    @Size(min = 1)
    private String title;

    private String content;

    @NotNull
    @ManyToOne
    private String status;

    @JsonProperty("assignee_id")
    private Long assigneeId;

    private List<Long> taskLabelIds;
}
