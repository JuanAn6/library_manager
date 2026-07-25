package com.library.manager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
@EntityListeners(AuditingEntityListener.class)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Bibliographic data ---
    @Column(nullable = false)
    private String title;

    @Column(unique = true, length = 20)
    private String isbn;

    // A book can be written by several authors, and an author can write several books
    @ManyToMany
    @JoinTable(
            name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @Column(length = 40)
    private String language;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(length = 2000)
    private String description;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "published_date")
    private LocalDate publishedDate;

    // --- Copy tracking ---
    @Column(name = "total_copies", nullable = false)
    private int totalCopies = 1;

    @Column(name = "available_copies", nullable = false)
    private int availableCopies = 1;

    // Physical location within the library
    private String location;

    // Kept for backward compatibility; reflects whether at least one copy is available
    private boolean available = true;

    // Loans registered for this book
    @OneToMany(mappedBy = "book")
    @JsonIgnore
    private Set<Loan> loans = new HashSet<>();

    // --- Auditing ---
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Book() {}

    public Book(String title, String isbn, LocalDate publishedDate) {
        this.title = title;
        this.isbn = isbn;
        this.publishedDate = publishedDate;
    }

    // Keeps 'available' consistent with the number of available copies
    @PrePersist
    @PreUpdate
    private void syncAvailability() {
        this.available = this.availableCopies > 0;
    }

    // --- Getters and setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public Set<Author> getAuthors() { return authors; }
    public void setAuthors(Set<Author> authors) { this.authors = authors; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Publisher getPublisher() { return publisher; }
    public void setPublisher(Publisher publisher) { this.publisher = publisher; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public LocalDate getPublishedDate() { return publishedDate; }
    public void setPublishedDate(LocalDate publishedDate) { this.publishedDate = publishedDate; }

    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }

    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public Set<Loan> getLoans() { return loans; }
    public void setLoans(Set<Loan> loans) { this.loans = loans; }

    // Read-only getters (managed automatically, no public setters)
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
