package hexlet.code.specification;

import hexlet.code.dto.task.TaskParamsDTO;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class TaskSpecification {

    public Specification<Task> build(TaskParamsDTO params) {
        return withTitleContain(params.getTitleCont())
                .and(withAssigneeId(params.getAssigneeId()))
                .and(withStatus(params.getStatus()))
                .and(withLabelId(params.getLabelId()));
    }

    private Specification<Task> withTitleContain(String title) {
        return (root, query, cb) -> {
            if (title == null) {
                return cb.conjunction();
            }

            return cb.like(cb.lower(root.get("name")), "%" + title.toLowerCase(Locale.ROOT) + "%");
        };
    }

    private Specification<Task> withAssigneeId(Long assigneeId) {
        return (root, query, cb) -> {
            if (assigneeId == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("assignee").get("id"), assigneeId);
        };
    }

    private Specification<Task> withStatus(String status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }

            return cb.equal(cb.lower(root.get("taskStatus").get("slug")), status.toLowerCase(Locale.ROOT));
        };
    }

    private Specification<Task> withLabelId(Long labelId) {
        return (root, query, cb) -> {
            if (labelId == null) {
                return cb.conjunction();
            }

            if (query != null) {
                query.distinct(true);
            }

            Join<Task, Label> taskLabelJoin = root.join("labels", JoinType.INNER);
            return cb.equal(taskLabelJoin.get("id"), labelId);
        };
    }
}
