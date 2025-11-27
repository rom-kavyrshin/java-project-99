package hexlet.code.app.controllers;

import hexlet.code.app.dto.label.LabelCreateDTO;
import hexlet.code.app.dto.label.LabelDTO;
import hexlet.code.app.dto.label.LabelUpdateDTO;
import hexlet.code.app.service.LabelsService;
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
@RequestMapping("/api/labels")
public class LabelsController {

    private final LabelsService labelsService;

    public LabelsController(LabelsService labelsService) {
        this.labelsService = labelsService;
    }

    @GetMapping(path = "")
    public List<LabelDTO> index() {
        return labelsService.getAll();
    }

    @GetMapping(path = "/{id}")
    public LabelDTO show(@PathVariable long id) {
        return labelsService.getById(id);
    }

    @PostMapping(path = "")
    @ResponseStatus(HttpStatus.CREATED)
    public LabelDTO create(@RequestBody LabelCreateDTO labelCreateDTO) {
        return labelsService.create(labelCreateDTO);
    }

    @PutMapping(path = "/{id}")
    public LabelDTO update(@PathVariable long id, @RequestBody LabelUpdateDTO labelUpdateDTO) {
        return labelsService.update(id, labelUpdateDTO);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        labelsService.delete(id);
    }
}
