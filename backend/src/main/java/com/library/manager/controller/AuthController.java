package com.library.manager.controller;

import com.library.manager.model.Role;
import com.library.manager.model.User;
import com.library.manager.repository.RoleRepository;
import com.library.manager.repository.UserRepository;
import com.library.manager.security.JwtCookieService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * Role given to every account created through the public register form.
     * Anything above MEMBER has to be granted deliberately, so signing up can
     * never hand out permission to edit the catalogue.
     */
    private static final String DEFAULT_ROLE = Role.Names.MEMBER;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtCookieService jwtCookieService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          JwtCookieService jwtCookieService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtCookieService = jwtCookieService;
    }

    // --- Request and response bodies ---
    // Records: immutable, and Jackson maps the JSON onto them with no boilerplate.

    public record Credentials(String username, String password) {}

    public record UserResponse(String username, String role) {}

    // POST /api/auth/register -> creates the account and signs the user in
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody Credentials request) {
        String username = request.username() == null ? "" : request.username().trim();
        String password = request.password() == null ? "" : request.password();

        if (username.length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username must be at least 3 characters long");
        }
        if (password.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters long");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That username is already taken");
        }

        Role role = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalStateException("Role " + DEFAULT_ROLE + " is missing from the database"));

        // Only the hash is ever stored, never the password itself.
        userRepository.save(new User(username, passwordEncoder.encode(password), role));

        // Signing in right away saves the user from typing the credentials
        // twice, and reuses the exact same code path as a normal login.
        return authenticate(username, password, HttpStatus.CREATED);
    }

    // POST /api/auth/login -> sets the HttpOnly cookie holding the JWT
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody Credentials request) {
        return authenticate(request.username(), request.password(), HttpStatus.OK);
    }

    // POST /api/auth/logout -> overwrites the cookie with an expired one
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // A JWT cannot be revoked once issued, so "logging out" means dropping
        // the browser's copy. The token stays valid until it expires, which is
        // why the lifetime is kept short.
        ResponseCookie cookie = jwtCookieService.buildExpiredCookie();
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    // GET /api/auth/me -> who the current cookie belongs to
    // The frontend cannot read an HttpOnly cookie, so this is how it finds out
    // whether there is a session at all. Reaching this method already means the
    // token was valid: the filter chain rejects the request otherwise.
    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return new UserResponse(authentication.getName(), jwtCookieService.roleOf(authentication));
    }

    private ResponseEntity<UserResponse> authenticate(String username, String password, HttpStatus status) {
        Authentication authentication;
        try {
            // Delegates to AppUserDetailsService plus the PasswordEncoder: we
            // never compare passwords ourselves.
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException ex) {
            // Deliberately vague: telling the caller which half was wrong would
            // let them find out which usernames exist.
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password", ex);
        }

        ResponseCookie cookie = jwtCookieService.buildCookie(jwtCookieService.issueToken(authentication));

        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new UserResponse(authentication.getName(), jwtCookieService.roleOf(authentication)));
    }
}
