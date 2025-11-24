package hexlet.code.app.repositories;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseJpaRepository<T, ID> extends JpaRepository<T, ID> {

    default void deleteByIdOrThrow(ID id, RuntimeException exception) {
        try {
            deleteById(id);
        } catch (DataIntegrityViolationException ignore) {
            throw exception;
        }
    }
}
