package com.library.manager.repository;

import com.library.manager.model.Author;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    List<Author> findByLastNameContainingIgnoreCase(String lastName);

    List<Author> findByNationalityIgnoreCase(String nationality);
}
