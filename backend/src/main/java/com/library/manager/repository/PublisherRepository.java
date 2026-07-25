package com.library.manager.repository;

import com.library.manager.model.Publisher;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {

    Optional<Publisher> findByNameIgnoreCase(String name);
}
