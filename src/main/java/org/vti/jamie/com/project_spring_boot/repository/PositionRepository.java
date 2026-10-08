package org.vti.jamie.com.project_spring_boot.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.vti.jamie.com.project_spring_boot.entity.Position;
import java.util.Optional;
import org.vti.jamie.com.project_spring_boot.enums.PositionName;

public interface PositionRepository extends JpaRepository<Position, Short> {
    // Deleted names remain reserved so that restore cannot violate the unique constraint.
    boolean existsByName(PositionName name);
    boolean existsByNameAndIdNot(PositionName name, Short id);
    Optional<Position> findByIdAndDeletedAtIsNull(Short id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Position d WHERE d.id = :id")
    Optional<Position> findByIdForUpdate(@Param("id") Short id);

    @Query("""
        SELECT d FROM Position d
        WHERE ((:deleted = true AND d.deletedAt IS NOT NULL)
            OR (:deleted = false AND d.deletedAt IS NULL))
          AND (:keyword IS NULL OR LOWER(cast(d.name as string)) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<Position> search(@Param("keyword") String keyword,
                            @Param("deleted") boolean deleted, Pageable pageable);
}
