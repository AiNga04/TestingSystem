package org.vti.jamie.com.project_spring_boot.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "GroupAccount")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupAccount {

    @EmbeddedId
    private GroupAccountId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("groupId")
    @JoinColumn(name = "GroupID", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("accountId")
    @JoinColumn(name = "AccountID", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Account account;

    @Column(name = "JoinDate", updatable = false)
    private LocalDateTime joinedAt;

    public GroupAccount(Group group, Account account) {
        this.group = Objects.requireNonNull(group);
        this.account = Objects.requireNonNull(account);
        if (group.getId() == null || account.getId() == null) {
            throw new IllegalArgumentException("Group and Account must be persisted before adding membership");
        }
        this.id = new GroupAccountId(
                group.getId(),
                account.getId()
        );
    }

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
    }
}
