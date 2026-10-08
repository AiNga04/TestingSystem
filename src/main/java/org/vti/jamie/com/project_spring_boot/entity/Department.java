package org.vti.jamie.com.project_spring_boot.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "Department")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DepartmentID", columnDefinition = "TINYINT UNSIGNED")
    private Short id;

    @Column(name = "DepartmentName",
            nullable = false, unique = true, length = 30)
    private String name;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    @Setter(AccessLevel.NONE)
    private Set<Account> accounts = new HashSet<>();

    public Department(String name) {
        this.name = Objects.requireNonNull(name);
    }
}
