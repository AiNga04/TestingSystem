package org.vti.jamie.com.project_spring_boot.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.vti.jamie.com.project_spring_boot.entity.Group;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Short> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Short id);

    @EntityGraph(attributePaths = "creator")
    Optional<Group> findByIdAndDeletedAtIsNull(Short id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM Group g WHERE g.id = :id")
    Optional<Group> findByIdForUpdate(@Param("id") Short id);

    @EntityGraph(attributePaths = "creator")
    @Query("""
        SELECT g FROM Group g
        WHERE ((:deleted = true AND g.deletedAt IS NOT NULL) OR (:deleted = false AND g.deletedAt IS NULL))
          AND (:keyword IS NULL OR LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:creatorId IS NULL OR g.creator.id = :creatorId)
        """)
    Page<Group> search(@Param("keyword") String keyword, @Param("deleted") boolean deleted,
                       @Param("creatorId") Short creatorId, Pageable pageable);
}
