package com.library.manager.controller;

import com.library.manager.model.Publisher;
import com.library.manager.repository.PublisherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/publishers")
public class PublisherController {

    private final PublisherRepository publisherRepository;

    public PublisherController(PublisherRepository publisherRepository) {
        this.publisherRepository = publisherRepository;
    }

    // GET /api/publishers -> index()
    @GetMapping
    public List<Publisher> index() {
        return publisherRepository.findAll();
    }

    // GET /api/publishers/{id} -> show()
    @GetMapping("/{id}")
    public Publisher show(@PathVariable Long id) {
        return publisherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publisher not found"));
    }

    // POST /api/publishers -> store()
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Publisher store(@RequestBody Publisher publisher) {
        return publisherRepository.save(publisher);
    }

    // PUT /api/publishers/{id} -> update()
    @PutMapping("/{id}")
    public Publisher update(@PathVariable Long id, @RequestBody Publisher publisherDetails) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publisher not found"));

        publisher.setName(publisherDetails.getName());
        publisher.setCountry(publisherDetails.getCountry());
        publisher.setWebsite(publisherDetails.getWebsite());

        return publisherRepository.save(publisher);
    }

    // DELETE /api/publishers/{id} -> destroy()
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable Long id) {
        if (!publisherRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Publisher not found");
        }
        publisherRepository.deleteById(id);
    }
}
