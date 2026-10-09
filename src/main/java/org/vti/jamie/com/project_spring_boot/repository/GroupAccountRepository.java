package org.vti.jamie.com.project_spring_boot.repository;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.vti.jamie.com.project_spring_boot.entity.*;
import java.util.Optional;

public interface GroupAccountRepository extends JpaRepository<GroupAccount, GroupAccountId> {
    @EntityGraph(attributePaths = {"group", "account", "account.department", "account.position"})
    @Query("""
        SELECT m FROM GroupAccount m WHERE m.group.id = :groupId
          AND m.group.deletedAt IS NULL AND m.account.deletedAt IS NULL
          AND (:keyword IS NULL OR LOWER(m.account.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(m.account.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<GroupAccount> search(@Param("groupId") Short groupId, @Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"group", "account", "account.department", "account.position"})
    @Query("""
        SELECT m FROM GroupAccount m WHERE m.id = :id
          AND m.group.deletedAt IS NULL AND m.account.deletedAt IS NULL
        """)
    Optional<GroupAccount> findActive(@Param("id") GroupAccountId id);
}
