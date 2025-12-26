package hexlet.code.repositories;

import hexlet.code.model.Label;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LabelRepository extends BaseJpaRepository<Label, Long> {
    Optional<Label> findByName(String name);
}
