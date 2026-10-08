package org.vti.jamie.com.project_spring_boot.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Answer")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AnswerID", columnDefinition = "TINYINT UNSIGNED")
    private Short id;

    @Column(name = "Content", nullable = false, length = 100)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "QuestionID", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Question question;

    @Column(name = "isCorrect")
    private Boolean correct = true;

    public Answer(String content, Boolean correct) {
        this.content = content;
        this.correct = correct;
    }
}
