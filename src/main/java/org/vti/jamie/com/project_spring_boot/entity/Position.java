package org.vti.jamie.com.project_spring_boot.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vti.jamie.com.project_spring_boot.enums.PositionName;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "`Position`")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PositionID", columnDefinition = "TINYINT UNSIGNED")
    private Short id;

    @Convert(converter = PositionNameConverter.class)
    @Column(name = "PositionName", nullable = false, unique = true)
    private PositionName name;

    @OneToMany(mappedBy = "position", fetch = FetchType.LAZY)
    @Setter(AccessLevel.NONE)
    private Set<Account> accounts = new HashSet<>();
}
