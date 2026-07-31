package com.library.manager.security;

import com.library.manager.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * The bridge between our "users" table and Spring Security. The
 * AuthenticationManager calls this during login to look the account up; it then
 * compares the submitted password against the stored hash by itself.
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * @param identifier what was typed in the sign-in form: a username OR an
     *                   email. The parameter is named "username" in the
     *                   interface, but nothing forces it to be one, so we accept
     *                   either and let the database decide which column matched.
     */
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        com.library.manager.model.User user = userRepository
                .findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user"));

        // Always the username, never the identifier that was typed: from here on
        // the principal (and the JWT subject built from it) has one single form,
        // whichever way the user signed in.
        //
        // roles(...) prefixes the name with "ROLE_", which is the convention
        // hasRole("ADMIN") expects. We store it unprefixed.
        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .disabled(!user.isEnabled())
                .roles(user.getRole().getName())
                .build();
    }
}
