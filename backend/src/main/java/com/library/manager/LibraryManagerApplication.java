package com.library.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LibraryManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(LibraryManagerApplication.class, args);
    }
}