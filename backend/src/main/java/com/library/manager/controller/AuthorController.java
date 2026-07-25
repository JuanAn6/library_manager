package com.library.manager.controller;

import com.library.manager.model.Author;
import com.library.manager.repository.AuthorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorRepository authorRepository;

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
            return authorRepository.findByLastNameContainingIgnoreCase(lastName);
        }
        if (nationality != null) {
            return authorRepository.findByNationalityIgnoreCase(nationality);
        }
        return authorRepository.findAll();
    }

    // GET /api/authors/{id} -> show()
    @GetMapping("/{id}")
    public Author show(@PathVariable Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));
    }

    // POST /api/authors -> store()
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Author store(@RequestBody Author author) {
        return authorRepository.save(author);
    }

    // PUT /api/authors/{id} -> update()
    @PutMapping("/{id}")
    public Author update(@PathVariable Long id, @RequestBody Author authorDetails) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));

        author.setFirstName(authorDetails.getFirstName());
        author.setLastName(authorDetails.getLastName());
        author.setNationality(authorDetails.getNationality());
        author.setBirthDate(authorDetails.getBirthDate());
        author.setBiography(authorDetails.getBiography());

        return authorRepository.save(author);
    }

    // DELETE /api/authors/{id} -> destroy()
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable Long id) {
        if (!authorRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found");
        }
        authorRepository.deleteById(id);
    }
}
