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
import org.springframework.http.ResponseEntity;
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
    // The body is copied field by field into a NEW entity: an id sent by the
    // client would otherwise turn this "create" into an overwrite of whatever
    // row already holds it.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book store(@RequestBody Book request) {
        Book book = new Book();
        apply(request, book);
        return bookRepository.save(book);
    }

    // PUT /api/books/{id} -> update()
    @PutMapping("/{id}")
    public Book update(@PathVariable Long id, @RequestBody Book bookDetails) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        apply(bookDetails, book);
        return bookRepository.save(book);
    }

    /**
     * Validates the incoming values and copies them onto the target entity.
     *
     * <p>Shared by store() and update() on purpose: a book must not be able to
     * be edited into a state that creating it would have rejected.
     */
    private void apply(Book request, Book target) {
        String title = request.getTitle() == null ? "" : request.getTitle().trim();
        if (title.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }

        // Blank is stored as null, so "no ISBN" has a single representation.
        // That matters here more than elsewhere: the column is UNIQUE, and two
        // empty strings would clash while two nulls do not.
        String isbn = request.getIsbn() == null || request.getIsbn().isBlank()
                ? null
                : request.getIsbn().trim();

        // Only a clash with a DIFFERENT book is a conflict: keeping its own ISBN
        // is not a change at all. Checked here so it comes back as a worded 409
        // instead of a constraint violation turning into a 500.
        if (isbn != null) {
            bookRepository.findByIsbn(isbn).ifPresent(other -> {
                if (!other.getId().equals(target.getId())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "That ISBN is already registered");
                }
            });
        }

        if (request.getTotalCopies() < 0 || request.getAvailableCopies() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The number of copies cannot be negative");
        }
        // The available ones are a subset of the ones the library owns, so the
        // stock would be telling us a lending had produced copies.
        if (request.getAvailableCopies() > request.getTotalCopies()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "There cannot be more available copies than total copies");
        }

        target.setTitle(title);
        target.setIsbn(isbn);
        target.setAuthors(resolveAuthors(request.getAuthors()));
        target.setCategory(resolveCategory(request.getCategory()));
        target.setPublisher(resolvePublisher(request.getPublisher()));
        target.setLanguage(request.getLanguage());
        target.setPageCount(request.getPageCount());
        target.setDescription(request.getDescription());
        target.setCoverImageUrl(request.getCoverImageUrl());
        target.setPublishedDate(request.getPublishedDate());
        target.setTotalCopies(request.getTotalCopies());
        target.setAvailableCopies(request.getAvailableCopies());
        target.setLocation(request.getLocation());
        // "available" is not copied on purpose: the entity keeps it in step with
        // the available copies on its own (see Book.syncAvailability).
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

    public record ErrorResponse(String message) {}

    /**
     * Puts the reason of a rejected request into the response body, the same way
     * UserController does: Spring's default error page drops it unless
     * server.error.include-message is turned on, and that setting would apply to
     * every endpoint, including the messages of unexpected exceptions.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleRejected(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(new ErrorResponse(ex.getReason()));
    }
}
