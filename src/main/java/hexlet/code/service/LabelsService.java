package hexlet.code.service;

import hexlet.code.component.CustomValidator;
import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.dto.label.LabelUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.exception.UnableDeleteException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.repositories.LabelRepository;
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
        labelRepository.deleteByIdOrThrow(id, new UnableDeleteException("Can't delete label with id " + id));
    }
}
