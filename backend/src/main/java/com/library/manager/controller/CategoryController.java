package com.library.manager.controller;

import com.library.manager.model.Category;
import com.library.manager.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // GET /api/categories -> index()
    @GetMapping
    public List<Category> index() {
        return categoryRepository.findAll();
    }

    // GET /api/categories/{id} -> show()
    @GetMapping("/{id}")
    public Category show(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    // POST /api/categories -> store()
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category store(@RequestBody Category category) {
        return categoryRepository.save(category);
    }

    // PUT /api/categories/{id} -> update()
    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @RequestBody Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());

        return categoryRepository.save(category);
    }

    // DELETE /api/categories/{id} -> destroy()
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        }
        categoryRepository.deleteById(id);
    }
}
