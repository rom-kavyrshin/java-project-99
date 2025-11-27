package hexlet.code.app.service;

import hexlet.code.app.component.CustomValidator;
import hexlet.code.app.dto.label.LabelCreateDTO;
import hexlet.code.app.dto.label.LabelDTO;
import hexlet.code.app.dto.label.LabelUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.LabelMapper;
import hexlet.code.app.repositories.LabelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LabelsService {

    private final LabelRepository labelRepository;
    private final LabelMapper labelMapper;
    private final CustomValidator validator;

    public LabelsService(
            LabelRepository labelRepository,
            LabelMapper labelMapper,
            CustomValidator validator
    ) {
        this.labelRepository = labelRepository;
        this.labelMapper = labelMapper;
        this.validator = validator;
    }

    public List<LabelDTO> getAll() {
        return labelRepository.findAll().stream()
                .map(labelMapper::map)
                .toList();
    }

    public LabelDTO getById(long id) {
        return labelRepository.findById(id)
                .map(labelMapper::map)
                .orElseThrow(() -> new ResourceNotFoundException("Label with id " + id + " not found"));
    }

    public LabelDTO create(LabelCreateDTO labelCreateDTO) {
        validator.validate(labelCreateDTO);

        var label = labelMapper.map(labelCreateDTO);
        labelRepository.save(label);
        return labelMapper.map(label);
    }

    public LabelDTO update(long id, LabelUpdateDTO labelUpdateDTO) {
        validator.validate(labelUpdateDTO);

        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label with id " + id + " not found"));
        labelMapper.update(labelUpdateDTO, label);
        labelRepository.save(label);

        return labelMapper.map(label);
    }

    public void delete(long id) {
        labelRepository.deleteById(id);
    }
}