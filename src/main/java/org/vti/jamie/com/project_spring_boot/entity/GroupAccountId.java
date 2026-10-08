package org.vti.jamie.com.project_spring_boot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GroupAccountId implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "GroupID", columnDefinition = "TINYINT UNSIGNED")
    private Short groupId;

    @Column(name = "AccountID", columnDefinition = "TINYINT UNSIGNED")
    private Short accountId;
}