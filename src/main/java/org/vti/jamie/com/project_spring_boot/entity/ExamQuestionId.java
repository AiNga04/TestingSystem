package org.vti.jamie.com.project_spring_boot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ExamQuestionId implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "ExamID", columnDefinition = "TINYINT UNSIGNED")
    private Short examId;

    @Column(name = "QuestionID", columnDefinition = "TINYINT UNSIGNED")
    private Short questionId;
}
