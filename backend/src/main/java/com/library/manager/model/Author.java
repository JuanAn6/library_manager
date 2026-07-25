package com.library.manager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "authors")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(length = 60)
    private String nationality;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 2000)
    private String biography;

    // Inverse side of the Book <-> Author relationship
    @ManyToMany(mappedBy = "authors")
    @JsonIgnore
    private Set<Book> books = new HashSet<>();

    // Constructors
    public Author() {}

    public Author(String firstName, String lastName, String nationality, LocalDate birthDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationality = nationality;
        this.birthDate = birthDate;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }

    public Set<Book> getBooks() { return books; }
    public void setBooks(Set<Book> books) { this.books = books; }
}
