package hexlet.code.app.controllers;

import hexlet.code.app.dto.task_status.TaskStatusCreateDTO;
import hexlet.code.app.dto.task_status.TaskStatusDTO;
import hexlet.code.app.dto.task_status.TaskStatusUpdateDTO;
import hexlet.code.app.service.TaskStatusService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/task_statuses")
public class TaskStatusController {

    private final TaskStatusService taskStatusService;

    public TaskStatusController(TaskStatusService taskStatusService) {
        this.taskStatusService = taskStatusService;
    }

    @GetMapping(path = "")
    public List<TaskStatusDTO> index() {
        return taskStatusService.getAll();
    }

    @GetMapping(path = "/{id}")
    public TaskStatusDTO show(@PathVariable long id) {
        return taskStatusService.getById(id);
    }

    @PostMapping(path = "")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskStatusDTO create(@RequestBody TaskStatusCreateDTO taskStatusCreateDTO) {
        return taskStatusService.create(taskStatusCreateDTO);
    }

    @PutMapping(path = "/{id}")
    public TaskStatusDTO update(@PathVariable long id, @RequestBody TaskStatusUpdateDTO taskStatusUpdateDTO) {
        return taskStatusService.update(id, taskStatusUpdateDTO);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        taskStatusService.delete(id);
    }
}
