package org.vti.jamie.com.project_spring_boot.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Objects;

@Entity
@Table(name = "ExamQuestion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExamQuestion {

    @EmbeddedId
    private ExamQuestionId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("examId")
    @JoinColumn(name = "ExamID", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("questionId")
    @JoinColumn(name = "QuestionID", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Question question;

    public ExamQuestion(Exam exam, Question question) {
        this.exam = Objects.requireNonNull(exam);
        this.question = Objects.requireNonNull(question);

        this.id = new ExamQuestionId(
                exam.getId(),
                question.getId()
        );
    }
}
