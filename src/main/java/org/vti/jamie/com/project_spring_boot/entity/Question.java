package org.vti.jamie.com.project_spring_boot.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "Question")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QuestionID", columnDefinition = "TINYINT UNSIGNED")
    private Short id;

    @Column(name = "Content", nullable = false, length = 100)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CategoryID", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private QuestionCategory category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TypeID", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private QuestionType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CreatorID", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Account creator;

    @Column(name = "CreateDate", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "question",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Answer> answers = new ArrayList<>();

    @OneToMany(mappedBy = "question")
    private Set<ExamQuestion> examQuestions = new HashSet<>();

    public void addAnswer(Answer answer) {
        Objects.requireNonNull(answer);
        answers.add(answer);
        answer.setQuestion(this);
    }

    public void removeAnswer(Answer answer) {
        if (answers.remove(answer)) {
            answer.setQuestion(null);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}