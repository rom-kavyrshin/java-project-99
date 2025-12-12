package hexlet.code.app.service;

import hexlet.code.app.component.CustomValidator;
import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task.TaskDTO;
import hexlet.code.app.dto.task.TaskUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.repositories.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final CustomValidator validator;

    public TaskService(
            TaskRepository taskRepository,
            TaskMapper taskMapper,
            CustomValidator validator
    ) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.validator = validator;
    }

    @Transactional
    public List<TaskDTO> getAll() {
        return taskRepository.findAll().stream()
                .map(taskMapper::map)
                .toList();
    }

    @Transactional
    public TaskDTO getById(long id) {
        return taskRepository.findById(id)
                .map(taskMapper::map)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
    }

    public TaskDTO create(TaskCreateDTO taskCreateDTO) {
        validator.validate(taskCreateDTO);

        var task = taskMapper.map(taskCreateDTO);
        task = taskRepository.save(task);
        return taskMapper.map(task);
    }

    public TaskDTO update(long id, TaskUpdateDTO taskUpdateDTO) {
        validator.validate(taskUpdateDTO);

        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
        taskMapper.update(taskUpdateDTO, task);
        task = taskRepository.save(task);

        return taskMapper.map(task);
    }

    public void delete(long id) {
        taskRepository.deleteById(id);
    }
}
