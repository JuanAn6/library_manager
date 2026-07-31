package com.library.manager.controller;

import com.library.manager.model.Author;
import com.library.manager.repository.AuthorRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/**
 * The people who write the books in the catalogue.
 *
 * <p>Reading is open to any signed-in user and writing to ADMIN and MANAGER:
 * the rule lives in SecurityConfig, so the filter chain answers 403 before any
 * method here runs.
 */
@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorRepository authorRepository;

    // Surname first, because that is how a list of authors is read and looked
    // up. Declared once so every response comes back in the same order.
    private static final Sort BY_NAME = Sort.by("lastName", "firstName");

    public AuthorController(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    // GET /api/authors -> index()
    // Optional filters: ?lastName=..., ?nationality=...
    @GetMapping
    public List<Author> index(
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String nationality) {

        if (lastName != null) {
            return authorRepository.findByLastNameContainingIgnoreCase(lastName, BY_NAME);
        }
        if (nationality != null) {
            return authorRepository.findByNationalityIgnoreCase(nationality, BY_NAME);
        }
        return authorRepository.findAll(BY_NAME);
    }

    // GET /api/authors/{id} -> show()
    @GetMapping("/{id}")
    public Author show(@PathVariable Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));
    }

    // POST /api/authors -> store()
    // The body is copied field by field into a NEW entity: an id sent by the
    // client would otherwise turn this "create" into an overwrite of whatever
    // row already holds it.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Author store(@RequestBody Author request) {
        Author author = new Author();
        apply(request, author);
        return authorRepository.save(author);
    }

    // PUT /api/authors/{id} -> update()
    @PutMapping("/{id}")
    public Author update(@PathVariable Long id, @RequestBody Author request) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));

        apply(request, author);
        return authorRepository.save(author);
    }

    // DELETE /api/authors/{id} -> destroy()
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));

        // Removing an author who still writes books would either break the join
        // table or silently leave those books with nobody credited. Saying so is
        // more useful than a constraint violation turning into a 500.
        if (!author.getBooks().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This author still has books in the catalogue");
        }

        authorRepository.delete(author);
    }

    /**
     * Validates the incoming values and copies them onto the target entity.
     *
     * <p>Shared by store() and update() on purpose: an author must not be able
     * to be edited into a state that creating it would have rejected.
     */
    private void apply(Author request, Author target) {
        String firstName = trimmed(request.getFirstName());
        if (firstName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "First name is required");
        }

        String lastName = trimmed(request.getLastName());
        if (lastName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Last name is required");
        }

        LocalDate birthDate = request.getBirthDate();
        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The birth date cannot be in the future");
        }

        target.setFirstName(firstName);
        target.setLastName(lastName);
        // The optional fields are stored as null when left empty, so "unknown"
        // has a single representation instead of also being the empty string.
        target.setNationality(trimmed(request.getNationality()));
        target.setBiography(trimmed(request.getBiography()));
        target.setBirthDate(birthDate);
    }

    /** Trims the text and turns anything blank into null. */
    private String trimmed(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
