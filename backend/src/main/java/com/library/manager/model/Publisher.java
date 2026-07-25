package com.library.manager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "publishers")
public class Publisher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(length = 80)
    private String country;

    @Column(length = 255)
    private String website;

    @OneToMany(mappedBy = "publisher")
    @JsonIgnore
    private Set<Book> books = new HashSet<>();

    // Constructors
    public Publisher() {}

    public Publisher(String name, String country, String website) {
        this.name = name;
        this.country = country;
        this.website = website;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public Set<Book> getBooks() { return books; }
    public void setBooks(Set<Book> books) { this.books = books; }
}
