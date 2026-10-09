package org.vti.jamie.com.project_spring_boot.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.vti.jamie.com.project_spring_boot.entity.Account;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Short> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);

    @EntityGraph(attributePaths = {"department", "position"})
    Optional<Account> findByIdAndDeletedAtIsNull(Short id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Short id);

    @EntityGraph(attributePaths = {"department", "position"})
    @Query("""
        SELECT a FROM Account a
        WHERE ((:deleted = true AND a.deletedAt IS NOT NULL) OR (:deleted = false AND a.deletedAt IS NULL))
          AND (:keyword IS NULL OR LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(a.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:departmentId IS NULL OR a.department.id = :departmentId)
          AND (:positionId IS NULL OR a.position.id = :positionId)
        """)
    Page<Account> search(@Param("keyword") String keyword, @Param("deleted") boolean deleted,
                         @Param("departmentId") Short departmentId, @Param("positionId") Short positionId,
                         Pageable pageable);
}
