package org.vti.jamie.com.project_spring_boot.entity;

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

    @Column(name = "DeletedAt")
    @Setter(AccessLevel.NONE)
    private LocalDateTime deletedAt;

    public Group(String name, Account creator) {
        this.name = Objects.requireNonNull(name);
        this.creator = Objects.requireNonNull(creator);
    }

    public void softDelete() {
        if (deletedAt == null) deletedAt = LocalDateTime.now();
    }

    public void restore() {
        deletedAt = null;
    }

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
