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

    // GET /api/books -> index()
    // Optional filters: ?available=true, ?title=..., ?category=..., ?author=...
    @GetMapping
    public List<Book> index(
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String author) {

        if (Boolean.TRUE.equals(available)) {
            return bookRepository.findByAvailableTrue();
        }
        if (title != null) {
            return bookRepository.findByTitleContainingIgnoreCase(title);
        }
        if (category != null) {
            return bookRepository.findByCategory_NameIgnoreCase(category);
        }
        if (author != null) {
            return bookRepository.findByAuthors_LastNameContainingIgnoreCase(author);
        }
        return bookRepository.findAll();
    }

    // GET /api/books/{id} -> show()
    @GetMapping("/{id}")
    public Book show(@PathVariable Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
    }

    // POST /api/books -> store()
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book store(@RequestBody Book book) {
        return bookRepository.save(book);
    }

    // PUT /api/books/{id} -> update()
    @PutMapping("/{id}")
    public Book update(@PathVariable Long id, @RequestBody Book bookDetails) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        book.setTitle(bookDetails.getTitle());
        book.setIsbn(bookDetails.getIsbn());
        book.setAuthors(bookDetails.getAuthors());
        book.setCategory(bookDetails.getCategory());
        book.setPublisher(bookDetails.getPublisher());
        book.setLanguage(bookDetails.getLanguage());
        book.setPageCount(bookDetails.getPageCount());
        book.setDescription(bookDetails.getDescription());
        book.setCoverImageUrl(bookDetails.getCoverImageUrl());
        book.setPublishedDate(bookDetails.getPublishedDate());
        book.setTotalCopies(bookDetails.getTotalCopies());
        book.setAvailableCopies(bookDetails.getAvailableCopies());
        book.setLocation(bookDetails.getLocation());

        return bookRepository.save(book);
    }

    // DELETE /api/books/{id} -> destroy()
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        }
        bookRepository.deleteById(id);
    }
}
