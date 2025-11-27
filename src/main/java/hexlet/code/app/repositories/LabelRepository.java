package hexlet.code.app.repositories;

import hexlet.code.app.model.Label;
import org.springframework.stereotype.Repository;

@Repository
public interface LabelRepository extends BaseJpaRepository<Label, Long> {
}