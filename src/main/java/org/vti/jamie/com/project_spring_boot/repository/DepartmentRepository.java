package org.vti.jamie.com.project_spring_boot.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.vti.jamie.com.project_spring_boot.entity.Department;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Short> {
    // Deleted names remain reserved so that restore cannot violate the unique constraint.
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Short id);
    Optional<Department> findByIdAndDeletedAtIsNull(Short id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Department d WHERE d.id = :id")
    Optional<Department> findByIdForUpdate(@Param("id") Short id);

    @Query("""
        SELECT d FROM Department d
        WHERE ((:deleted = true AND d.deletedAt IS NOT NULL)
            OR (:deleted = false AND d.deletedAt IS NULL))
          AND (:keyword IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<Department> search(@Param("keyword") String keyword,
                            @Param("deleted") boolean deleted, Pageable pageable);
}
