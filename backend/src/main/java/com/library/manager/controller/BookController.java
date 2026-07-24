package com.library.manager.controller;

import com.library.manager.model.Book;
import com.library.manager.repository.BookRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookRepository bookRepository;

    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // GET /api/books  -> index()
    @GetMapping
    public List<Book> index() {
        return bookRepository.findAll();
    }

    // GET /api/books/{id}  -> show()
    @GetMapping("/{id}")
    public Book show(@PathVariable Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado"));
    }

    // POST /api/books  -> store()
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book store(@RequestBody Book book) {
        return bookRepository.save(book);
    }

    // PUT /api/books/{id}  -> update()
    @PutMapping("/{id}")
    public Book update(@PathVariable Long id, @RequestBody Book bookDetails) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado"));

        book.setTitle(bookDetails.getTitle());
        book.setAuthor(bookDetails.getAuthor());
        book.setIsbn(bookDetails.getIsbn());
        book.setPublishedDate(bookDetails.getPublishedDate());
        book.setAvailable(bookDetails.isAvailable());

        return bookRepository.save(book);
    }

    // DELETE /api/books/{id}  -> destroy()
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado");
        }
        bookRepository.deleteById(id);
    }
}