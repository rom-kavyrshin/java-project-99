package hexlet.code.service;

import hexlet.code.component.CustomValidator;
import hexlet.code.dto.task_status.TaskStatusCreateDTO;
import hexlet.code.dto.task_status.TaskStatusDTO;
import hexlet.code.dto.task_status.TaskStatusUpdateDTO;
import hexlet.code.exception.UnableDeleteException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.repositories.TaskStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskStatusService {

    private final TaskStatusRepository taskStatusRepository;
    private final TaskStatusMapper taskStatusMapper;
    private final CustomValidator validator;

    public TaskStatusService(
            TaskStatusRepository taskStatusRepository,
            TaskStatusMapper taskStatusMapper,
            CustomValidator validator
    ) {
        this.taskStatusRepository = taskStatusRepository;
        this.taskStatusMapper = taskStatusMapper;
        this.validator = validator;
    }

    public List<TaskStatusDTO> getAll() {
        return taskStatusRepository.findAll().stream()
                .map(taskStatusMapper::map)
                .toList();
    }

    public TaskStatusDTO getById(long id) {
        return taskStatusRepository.findById(id)
                .map(taskStatusMapper::map)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with id " + id + " not found"));
    }

    public TaskStatusDTO create(TaskStatusCreateDTO taskCreateDTO) {
        validator.validate(taskCreateDTO);

        var taskStatus = taskStatusMapper.map(taskCreateDTO);
        taskStatusRepository.save(taskStatus);
        return taskStatusMapper.map(taskStatus);
    }

    public TaskStatusDTO update(long id, TaskStatusUpdateDTO taskStatusUpdateDTO) {
        validator.validate(taskStatusUpdateDTO);

        var taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with id " + id + " not found"));
        taskStatusMapper.update(taskStatusUpdateDTO, taskStatus);
        taskStatusRepository.save(taskStatus);

        return taskStatusMapper.map(taskStatus);
    }

    public void delete(long id) {
        taskStatusRepository.deleteByIdOrThrow(id, new UnableDeleteException("Can't delete TaskStatus with id " + id));
    }
}
