package com.library.manager.controller;

import com.library.manager.model.Author;
import com.library.manager.model.Book;
import com.library.manager.model.Category;
import com.library.manager.model.Publisher;
import com.library.manager.repository.AuthorRepository;
import com.library.manager.repository.BookRepository;
import com.library.manager.repository.CategoryRepository;
import com.library.manager.repository.PublisherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;

    public BookController(BookRepository bookRepository,
                          AuthorRepository authorRepository,
                          CategoryRepository categoryRepository,
                          PublisherRepository publisherRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.publisherRepository = publisherRepository;
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
        book.setAuthors(resolveAuthors(bookDetails.getAuthors()));
        book.setCategory(resolveCategory(bookDetails.getCategory()));
        book.setPublisher(resolvePublisher(bookDetails.getPublisher()));
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

    // The client only picks authors from a dropdown, so the payload is trusted
    // for its ids alone: every author is reloaded from the database and the
    // rest of the incoming fields is discarded. This keeps a partial or stale
    // author object in the request from overwriting the stored one.
    private Set<Author> resolveAuthors(Set<Author> incoming) {
        if (incoming == null || incoming.isEmpty()) {
            return new HashSet<>();
        }

        List<Long> ids = incoming.stream()
                .map(Author::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Set<Author> found = new HashSet<>(authorRepository.findAllById(ids));
        if (found.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown author in the request");
        }
        return found;
    }

    // Same rule as resolveAuthors: only the id of the incoming category is
    // trusted. A null category is legitimate, it means "no category".
    private Category resolveCategory(Category incoming) {
        if (incoming == null || incoming.getId() == null) {
            return null;
        }
        return categoryRepository.findById(incoming.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown category in the request"));
    }

    // Same rule as resolveCategory, for the publisher relation.
    private Publisher resolvePublisher(Publisher incoming) {
        if (incoming == null || incoming.getId() == null) {
            return null;
        }
        return publisherRepository.findById(incoming.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown publisher in the request"));
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
