package com.library.manager.repository;

import com.library.manager.model.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    /**
     * Looks an account up by either identifier, which is how signing in accepts
     * a username OR an email. Both columns are unique, so at most one row can
     * come back. Derived query: Spring Data writes
     * "where username = ?1 or email = ?2" from the method name.
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * Backs the search box of the users list: one term is tried against both
     * identifiers, so typing part of an address finds the account just as
     * typing part of the name does. Derived query, same as above, with
     * "containing ignore case" on each side.
     */
    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username, String email, Sort sort);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
