package com.library.manager.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * The role a {@link User} holds. One role per user, so this is the "one" side
 * of the relation.
 *
 * <p>The three rows are created by the V3 migration; see {@link Names} for the
 * values the rest of the code refers to.
 */
@Entity
@Table(name = "roles")
@EntityListeners(AuditingEntityListener.class)
public class Role {

    /**
     * The role names that exist, as constants instead of loose strings so a
     * typo is a compile error rather than a silent authorization hole.
     */
    public static final class Names {
        public static final String ADMIN = "ADMIN";
        public static final String MANAGER = "MANAGER";
        public static final String MEMBER = "MEMBER";

        private Names() {}
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String name;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Role() {}

    public Role(String name) {
        this.name = name;
    }

    // --- Getters and setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // Read-only: managed by the auditing listener
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
