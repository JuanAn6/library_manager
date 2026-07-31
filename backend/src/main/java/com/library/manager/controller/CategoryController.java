package com.library.manager.controller;

import com.library.manager.model.Category;
import com.library.manager.repository.CategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

/**
 * The subjects a book is filed under.
 *
 * <p>Reading is open to any signed-in user and writing to ADMIN and MANAGER:
 * the rule lives in SecurityConfig, so the filter chain answers 403 before any
 * method here runs.
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    // A category is only ever looked up by its name, so that is the order every
    // response comes back in. Declared once so the filtered list is sorted the
    // same way as the unfiltered one.
    private static final Sort BY_NAME = Sort.by("name");

    // Mirrors the column lengths declared on the entity. Checking them here
    // turns an oversized value into a 400 with a readable message, instead of
    // the database rejecting the INSERT and that surfacing as a 500.
    private static final int NAME_MAX_LENGTH = 80;
    private static final int DESCRIPTION_MAX_LENGTH = 500;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // GET /api/categories -> index()
    // Optional filter: ?name=...
    @GetMapping
    public List<Category> index(@RequestParam(required = false) String name) {
        if (name != null) {
            return categoryRepository.findByNameContainingIgnoreCase(name, BY_NAME);
        }
        return categoryRepository.findAll(BY_NAME);
    }

    // GET /api/categories/{id} -> show()
    @GetMapping("/{id}")
    public Category show(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    // POST /api/categories -> store()
    // The body is copied field by field into a NEW entity: an id sent by the
    // client would otherwise turn this "create" into an overwrite of whatever
    // row already holds it.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category store(@RequestBody Category request) {
        Category category = new Category();
        apply(request, category);
        return categoryRepository.save(category);
    }

    // PUT /api/categories/{id} -> update()
    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @RequestBody Category request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        apply(request, category);
        return categoryRepository.save(category);
    }

    // DELETE /api/categories/{id} -> destroy()
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        // Removing a category that books are still filed under would leave them
        // pointing at nothing. Saying so is more useful than a constraint
        // violation turning into a 500.
        if (!category.getBooks().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This category still has books in the catalogue");
        }

        categoryRepository.delete(category);
    }

    /**
     * Validates the incoming values and copies them onto the target entity.
     *
     * <p>Shared by store() and update() on purpose: a category must not be able
     * to be edited into a state that creating it would have rejected.
     */
    private void apply(Category request, Category target) {
        String name = trimmed(request.getName());
        if (name == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        if (name.length() > NAME_MAX_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The name cannot be longer than " + NAME_MAX_LENGTH + " characters");
        }

        // The column is unique, so a duplicate would be rejected anyway; looking
        // it up here is what turns that into a 409 the form can display.
        // Comparing the ids lets a category keep its own name when only the
        // description is being edited.
        Optional<Category> clash = categoryRepository.findByNameIgnoreCase(name);
        if (clash.isPresent() && !clash.get().getId().equals(target.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That category name is already taken");
        }

        String description = trimmed(request.getDescription());
        if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The description cannot be longer than " + DESCRIPTION_MAX_LENGTH + " characters");
        }

        target.setName(name);
        // The optional field is stored as null when left empty, so "no
        // description" has a single representation instead of also being the
        // empty string.
        target.setDescription(description);
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
     * AuthorController does: Spring's default error page drops it unless
     * server.error.include-message is turned on, and that setting would apply to
     * every endpoint, including the messages of unexpected exceptions.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleRejected(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(new ErrorResponse(ex.getReason()));
    }
}
