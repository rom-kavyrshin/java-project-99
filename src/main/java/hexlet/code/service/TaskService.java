package hexlet.code.service;

import hexlet.code.component.CustomValidator;
import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskParamsDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.repositories.TaskRepository;
import hexlet.code.specification.TaskSpecification;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final TaskSpecification taskSpecification;
    private final CustomValidator validator;

    public TaskService(
            TaskRepository taskRepository,
            TaskMapper taskMapper,
            TaskSpecification taskSpecification,
            CustomValidator validator
    ) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.taskSpecification = taskSpecification;
        this.validator = validator;
    }

    @Transactional
    public List<TaskDTO> getAll(TaskParamsDTO params) {
        return taskRepository.findAll(taskSpecification.build(params)).stream()
                .map(taskMapper::map)
                .toList();
    }

    @Transactional
    public TaskDTO getById(long id) {
        return taskRepository.findById(id)
                .map(taskMapper::map)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
    }

    @Transactional
    public TaskDTO create(TaskCreateDTO taskCreateDTO) {
        validator.validate(taskCreateDTO);

        var task = taskMapper.map(taskCreateDTO);
        task = taskRepository.save(task);
        return taskMapper.map(task);
    }

    @Transactional
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
