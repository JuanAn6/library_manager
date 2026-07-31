package com.library.manager.controller;

import com.library.manager.model.Role;
import com.library.manager.model.User;
import com.library.manager.repository.RoleRepository;
import com.library.manager.repository.UserRepository;
import com.library.manager.validation.EmailRules;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The accounts that can sign in.
 *
 * <p>Only ADMIN reaches this controller: the rule lives in SecurityConfig, so
 * the filter chain answers 403 before any method here runs.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * What the API exposes about a user. Deliberately a record instead of the
     * entity: the password hash and the role's own timestamps never reach the
     * client, and the response cannot start leaking new columns just because
     * someone adds a field to {@link User}.
     */
    public record UserSummary(Long id,
                              String username,
                              String email,
                              String role,
                              boolean enabled,
                              LocalDateTime createdAt) {

        static UserSummary of(User user) {
            return new UserSummary(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole().getName(),
                    user.isEnabled(),
                    user.getCreatedAt());
        }
    }

    // Sorted by username so the list has a stable order between requests.
    private static final Sort BY_USERNAME = Sort.by("username");

    // GET /api/users -> index()
    // Optional filter: ?search=... , matched against the username and the email
    @GetMapping
    public List<UserSummary> index(@RequestParam(required = false) String search) {
        // Filtering here rather than in the browser keeps the list correct once
        // there are more accounts than a single response can hold.
        List<User> users = search == null || search.isBlank()
                ? userRepository.findAll(BY_USERNAME)
                : userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        search.trim(), search.trim(), BY_USERNAME);

        return users.stream()
                .map(UserSummary::of)
                .toList();
    }

    // GET /api/users/{id} -> show()
    @GetMapping("/{id}")
    public UserSummary show(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(UserSummary::of)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    /**
     * What creating an account from this screen needs.
     *
     * <p>Unlike {@link UserUpdateRequest} this one carries a password: a brand
     * new account has to be able to sign in, and there is nothing to keep.
     */
    public record UserCreateRequest(String username, String email, String password, String role, Boolean enabled) {}

    // POST /api/users -> store()
    // Not the same thing as /api/auth/register: that one is public and always
    // creates a MEMBER, while an administrator creating an account here picks
    // the role, and is not signed in as the account they just created.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserSummary store(@RequestBody UserCreateRequest request) {
        String username = request.username() == null ? "" : request.username().trim();
        // Same floors as the register endpoint, so an account cannot be created
        // in a state the sign-up form would have rejected.
        if (username.length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username must be at least 3 characters long");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That username is already taken");
        }

        // Lowercased for the same reason as in AuthController: one address must
        // not be able to become two accounts by changing its capitals.
        String email = request.email() == null ? "" : request.email().trim().toLowerCase();
        if (!EmailRules.isValid(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid email address is required");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That email address is already registered");
        }

        String password = request.password() == null ? "" : request.password();
        if (password.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters long");
        }

        Role role = roleRepository.findByName(request.role() == null ? "" : request.role())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown role in the request"));

        // Only the hash is ever stored, never the password itself.
        User user = new User(username, email, passwordEncoder.encode(password), role);
        user.setEnabled(request.enabled() == null || request.enabled());

        return UserSummary.of(userRepository.save(user));
    }

    /**
     * What an administrator may change about an account.
     *
     * <p>The password is deliberately absent: changing it is a different
     * operation with its own rules, and folding it into a general update makes
     * it far too easy to reset somebody's password by accident.
     */
    public record UserUpdateRequest(String username, String email, String role, Boolean enabled) {}

    // PUT /api/users/{id} -> update()
    @PutMapping("/{id}")
    public UserSummary update(@PathVariable Long id,
                              @RequestBody UserUpdateRequest request,
                              Authentication authentication) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String username = request.username() == null ? "" : request.username().trim();
        // Same floor as the register endpoint, so an account cannot be edited
        // into a state the sign-up form would have rejected.
        if (username.length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username must be at least 3 characters long");
        }
        // Only a clash with a DIFFERENT account is a conflict: keeping your own
        // username is not a change at all.
        if (!username.equals(user.getUsername()) && userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That username is already taken");
        }

        // Lowercased for the same reason as in AuthController: one address must
        // not be able to become two accounts by changing its capitals.
        String email = request.email() == null ? "" : request.email().trim().toLowerCase();
        if (!EmailRules.isValid(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid email address is required");
        }
        if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That email address is already registered");
        }

        Role role = roleRepository.findByName(request.role() == null ? "" : request.role())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown role in the request"));

        boolean enabled = request.enabled() == null || request.enabled();

        // Guard against the one mistake there is no way back from: an
        // administrator removing their own access. With a single admin left,
        // demoting or disabling it would lock everyone out of this screen for
        // good, since only an ADMIN can reach it.
        if (user.getUsername().equals(authentication.getName())) {
            if (!role.getName().equals(user.getRole().getName())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "You cannot change your own role");
            }
            if (!enabled) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "You cannot disable your own account");
            }
        }

        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        user.setEnabled(enabled);

        return UserSummary.of(userRepository.save(user));
    }

    public record ErrorResponse(String message) {}

    /**
     * Puts the reason of a rejected request into the response body.
     *
     * <p>Spring's default error page drops it unless
     * server.error.include-message is turned on, and that setting applies to
     * every endpoint, including the messages of unexpected exceptions. Handling
     * it here keeps the leak surface to the reasons this controller writes
     * itself, which are worded for the user: "That username is already taken"
     * is the whole point of the check.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleRejected(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(new ErrorResponse(ex.getReason()));
    }
}
