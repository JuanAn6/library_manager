package com.library.manager.config;

import com.library.manager.model.Book;
import com.library.manager.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private final BookRepository bookRepository;

    public DataSeeder(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(String... args) {
        if (bookRepository.count() == 0) {
            List<Book> books = List.of(
                new Book("Cien años de soledad", "Gabriel García Márquez", "9780307474728", LocalDate.of(1967, 5, 30)),
                new Book("1984", "George Orwell", "9780451524935", LocalDate.of(1949, 6, 8)),
                new Book("El Quijote", "Miguel de Cervantes", "9788424116381", LocalDate.of(1605, 1, 16)),
                new Book("Fahrenheit 451", "Ray Bradbury", "9781451673319", LocalDate.of(1953, 10, 19)),
                new Book("Rayuela", "Julio Cortázar", "9788437604572", LocalDate.of(1963, 6, 28))
            );

            for (Book book : books) {
                Book saved = bookRepository.save(book);
                System.out.printf(" Creado: \"%s\" | createdAt=%s | updatedAt=%s%n", saved.getTitle(), saved.getCreatedAt(), saved.getUpdatedAt());
            }

            System.out.println("Seeder ejecutado: " + books.size() + " libros creados");
        } else {
            System.out.println("Ya hay libros en la base de datos, seeder omitido");
        }
    }
}