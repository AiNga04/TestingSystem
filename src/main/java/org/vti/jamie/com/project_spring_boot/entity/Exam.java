package org.vti.jamie.com.project_spring_boot.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "Exam")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ExamID", columnDefinition = "TINYINT UNSIGNED")
    private Short id;

    @Column(name = "Code", nullable = false,
            length = 10, updatable = false)
    private String code;

    @Column(name = "Title", nullable = false, length = 50)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CategoryID", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private QuestionCategory category;

    @Column(name = "Duration", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Short duration;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CreatorID", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Account creator;

    @Column(name = "CreateDate", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "exam",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<ExamQuestion> examQuestions = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
