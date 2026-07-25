package com.library.manager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "members")
@EntityListeners(AuditingEntityListener.class)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 30)
    private String phone;

    private String address;

    @Column(name = "membership_date")
    private LocalDate membershipDate;

    private boolean active = true;

    @OneToMany(mappedBy = "member")
    @JsonIgnore
    private Set<Loan> loans = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public Member() {}

    public Member(String firstName, String lastName, String email, String phone, LocalDate membershipDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.membershipDate = membershipDate;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDate getMembershipDate() { return membershipDate; }
    public void setMembershipDate(LocalDate membershipDate) { this.membershipDate = membershipDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Set<Loan> getLoans() { return loans; }
    public void setLoans(Set<Loan> loans) { this.loans = loans; }

    // Read-only getter (managed automatically, no public setter)
    public LocalDateTime getCreatedAt() { return createdAt; }
}
