package org.vti.jamie.com.project_spring_boot.repository;

import java.util.List;

public interface BaseRepository<T, ID> {

    void save(T entity);

    T findById(ID id);

    List<T> findAll();

    T update(T entity);

    void delete(ID id);
}
