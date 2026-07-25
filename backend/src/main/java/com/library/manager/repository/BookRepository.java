package com.library.manager.repository;

import com.library.manager.model.Book;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByAvailableTrue();

    List<Book> findByTitleContainingIgnoreCase(String keyword);

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByCategory_NameIgnoreCase(String categoryName);

    List<Book> findByAuthors_LastNameContainingIgnoreCase(String lastName);
}
