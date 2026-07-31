package com.library.manager.repository;

import com.library.manager.model.Author;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    // The Sort parameter is not part of the query: Spring Data strips it out and
    // appends the ORDER BY, so the filtered lists come back in the same order as
    // the unfiltered one.
    List<Author> findByLastNameContainingIgnoreCase(String lastName, Sort sort);

    List<Author> findByNationalityIgnoreCase(String nationality, Sort sort);
}
