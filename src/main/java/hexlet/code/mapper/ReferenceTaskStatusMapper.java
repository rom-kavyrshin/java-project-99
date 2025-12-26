package hexlet.code.mapper;

import hexlet.code.exception.DependentResourceNotFoundException;
import hexlet.code.model.TaskStatus;
import hexlet.code.repositories.TaskStatusRepository;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public abstract class ReferenceTaskStatusMapper {

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    public TaskStatus map(String statusSlug) {
        return taskStatusRepository
                .findBySlug(statusSlug)
                .orElseThrow(() ->
                        new DependentResourceNotFoundException(
                                "TaskStatus with slug " + statusSlug + " not found"
                        )
                );
    }
}
