package com.library.manager.repository;

import com.library.manager.model.Category;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameIgnoreCase(String name);

    // The Sort parameter is not part of the query: Spring Data strips it out and
    // appends the ORDER BY, so the filtered list comes back in the same order as
    // the unfiltered one.
    List<Category> findByNameContainingIgnoreCase(String name, Sort sort);
}
