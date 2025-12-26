package hexlet.code.mapper;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.exception.DependentResourceNotFoundException;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.repositories.LabelRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        uses = { JsonNullableMapper.class, NotNullReferenceMapper.class, ReferenceTaskStatusMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskMapper {

    private LabelRepository labelRepository;

    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
    @Mapping(source = "status", target = "taskStatus")
    @Mapping(source = "assigneeId", target = "assignee")
    @Mapping(source = "taskLabelIds", target = "labels", qualifiedByName = "mapLabelsIdsToLabels")
    public abstract Task map(TaskCreateDTO taskCreteDTO);

    @Mapping(source = "name", target = "title")
    @Mapping(source = "description", target = "content")
    @Mapping(source = "taskStatus.slug", target = "status")
    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "labels", target = "taskLabelIds", qualifiedByName = "mapLabelsToIds")
    public abstract TaskDTO map(Task task);

    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
    @Mapping(source = "status", target = "taskStatus")
    @Mapping(source = "assigneeId", target = "assignee")
    @Mapping(source = "taskLabelIds", target = "labels", qualifiedByName = "mapLabelsIdsToLabels")
    public abstract void update(TaskUpdateDTO updateDTO, @MappingTarget Task model);

    @Autowired
    public void setLabelRepository(LabelRepository labelRepository) {
        this.labelRepository = labelRepository;
    }

    @Named("mapLabelsToIds")
    public List<Long> mapLabelsToIds(List<Label> labels) {
        if (labels == null) {
            return null;
        }
        return labels.stream()
                .map(Label::getId)
                .collect(Collectors.toList());
    }

    @Named("mapLabelsIdsToLabels")
    public List<Label> mapLabelsIdsToLabels(List<Long> labelIds) {
        if (labelIds == null) {
            return Collections.emptyList();
        }
        List<Label> result = labelRepository.findAllById(labelIds);

        if (labelIds.size() != result.size()) {
            Set<Long> deleted = new HashSet<>(labelIds);
            deleted.removeAll(result.stream().map(Label::getId).collect(Collectors.toSet()));
            throw new DependentResourceNotFoundException(
                    "Labels with ids " + deleted + " not found"
            );
        }

        return result;
    }
}
