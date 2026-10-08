package org.vti.jamie.com.project_spring_boot.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.vti.jamie.com.project_spring_boot.entity.Department;

public interface DepartmentRepository
        extends JpaRepository<Department, Short> {

    boolean existsByNameIgnoreCase(
            String departmentName
    );

    boolean existsByNameIgnoreCaseAndIdNot(
            String departmentName,
            Short departmentId
    );

    @Query("""
        SELECT d
        FROM Department d
        WHERE :keyword IS NULL
           OR LOWER(d.name)
              LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
    Page<Department> search(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}