package hexlet.code.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Task implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private Long index;

    @NotNull
    @Column(nullable = false)
    @Size(min = 1)
    private String name;

    @Lob
    private String description;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private TaskStatus taskStatus;

    @JoinColumn(nullable = true)
    @ManyToOne(optional = true)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private User assignee;

    @CreatedDate
    private LocalDateTime createdAt;
}
