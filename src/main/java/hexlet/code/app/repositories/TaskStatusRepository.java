package hexlet.code.app.repositories;

import hexlet.code.app.model.TaskStatus;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskStatusRepository extends BaseJpaRepository<TaskStatus, Long> {
    Optional<TaskStatus> findBySlug(String slug);

}
