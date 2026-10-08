package org.vti.jamie.com.project_spring_boot.entity;

import jakarta.persistence.*;
import lombok.*;
import org.vti.jamie.com.project_spring_boot.enums.QuestionTypeName;

import java.util.*;

@Entity
@Table(name = "TypeQuestion")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TypeID", columnDefinition = "TINYINT UNSIGNED")
    private Short id;

    @Convert(converter = QuestionTypeConverter.class)
    @Column(name = "TypeName", nullable = false, unique = true)
    private QuestionTypeName name;

    @OneToMany(mappedBy = "type", fetch = FetchType.LAZY)
    private Set<Question> questions = new HashSet<>();
}