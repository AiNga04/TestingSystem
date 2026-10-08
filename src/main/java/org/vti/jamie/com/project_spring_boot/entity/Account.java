package org.vti.jamie.com.project_spring_boot.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "`Account`")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AccountID", columnDefinition = "TINYINT UNSIGNED")
    private Short id;

    @Column(name = "Email", nullable = false,
            unique = true, length = 50, updatable = false)
    private String email;

    @Column(name = "Username", nullable = false,
            unique = true, length = 50, updatable = false)
    private String username;

    @Column(name = "FullName", nullable = false, length = 50)
    private String fullName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DepartmentID", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PositionID", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Position position;

    @Column(name = "CreateDate", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    private Set<Group> createdGroups = new HashSet<>();

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Set<GroupAccount> groupMemberships = new HashSet<>();

    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    private Set<Question> createdQuestions = new HashSet<>();

    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    private Set<Exam> createdExams = new HashSet<>();

    public Account(
            String email,
            String username,
            String fullName,
            Department department,
            Position position
    ) {
        this.email = Objects.requireNonNull(email);
        this.username = Objects.requireNonNull(username);
        this.fullName = Objects.requireNonNull(fullName);
        this.department = Objects.requireNonNull(department);
        this.position = Objects.requireNonNull(position);
    }

    public void changeFullName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Invalid full name");
        }
        this.fullName = newName.trim();
    }

    public void changeDepartment(Department department) {
        this.department = Objects.requireNonNull(department);
    }

    public void changePosition(Position position) {
        this.position = Objects.requireNonNull(position);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
