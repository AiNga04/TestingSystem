package org.vti.jamie.com.project_spring_boot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "`Group`")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GroupID", columnDefinition = "TINYINT UNSIGNED")
    private Short id;

    @Column(name = "GroupName",
            nullable = false, unique = true, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatorID", columnDefinition = "TINYINT UNSIGNED")
    private Account creator;

    @Column(name = "CreateDate", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "group",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<GroupAccount> memberships = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
