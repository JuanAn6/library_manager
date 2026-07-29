package com.library.manager.config;

import com.library.manager.model.Role;
import com.library.manager.security.CookieBearerTokenResolver;
import com.library.manager.security.JwtCookieService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Everything that decides who may call what.
 *
 * <p>A request travels through the security filter chain BEFORE Spring MVC
 * picks a controller method:
 *
 * <pre>
 *   request
 *     -> CorsFilter                      is this origin allowed?
 *     -> BearerTokenAuthenticationFilter reads the cookie (see
 *                                        CookieBearerTokenResolver), validates
 *                                        the signature and the expiry, and puts
 *                                        an Authentication in the context
 *     -> AuthorizationFilter             checks the rules declared below
 *     -> DispatcherServlet               only now does BookController run
 * </pre>
 *
 * If the token is missing or invalid the chain stops at the second step with a
 * 401, and the controller is never reached. If the token is fine but the user
 * lacks the role, it stops at the third step with a 403.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String jwtSecret;
    private final String allowedOrigin;

    public SecurityConfig(@Value("${app.jwt.secret}") String jwtSecret,
                          @Value("${app.cors.allowed-origin}") String allowedOrigin) {
        this.jwtSecret = jwtSecret;
        this.allowedOrigin = allowedOrigin;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CookieBearerTokenResolver tokenResolver) throws Exception {
        return http
                // Must be declared here: the CorsFilter has to run inside the
                // chain, otherwise a 401 is returned without CORS headers and
                // the browser reports it as a CORS failure instead of a 401.
                .cors(Customizer.withDefaults())
                // No server-side session and no CSRF token: the cookie is
                // SameSite=Strict, so the browser never attaches it to a
                // request started by another site. See the note in
                // JwtCookieService.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Preflight requests carry no credentials by design
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // When a controller fails, MVC re-dispatches internally
                        // to /error to render the body, and that dispatch goes
                        // through this chain too. Without this line an
                        // anonymous 400 comes back to the caller as a 401.
                        .requestMatchers("/error").permitAll()
                        // Rules are read top to bottom and the FIRST match
                        // wins, so the public endpoints have to come first.
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        // The rest of /api/auth belongs to whoever is signed in,
                        // whatever their role. This has to come before the write
                        // rule below, or POST /api/auth/logout would be treated
                        // as a change to the catalogue and a MEMBER could not
                        // even sign out.
                        .requestMatchers("/api/auth/**").authenticated()
                        // Reading the catalogue: any signed-in user, MEMBER included
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                        // Changing it: staff only. A MEMBER borrows books, it
                        // does not edit the catalogue.
                        .requestMatchers("/api/**").hasAnyRole(Role.Names.ADMIN, Role.Names.MANAGER)
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .bearerTokenResolver(tokenResolver)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    /**
     * The delegating encoder stores hashes prefixed with the algorithm
     * ("{bcrypt}$2a$10$..."), which allows changing algorithm later without
     * invalidating the passwords already stored.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /** Used by the login endpoint to check a username/password pair. */
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    // --- JWT signing ---
    // HS256: one shared secret signs and verifies. Fine for a single service;
    // an asymmetric key would be the choice if a third party had to verify.

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey()));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(secretKey()).macAlgorithm(MacAlgorithm.HS256).build();
    }

    private SecretKeySpec secretKey() {
        return new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    /**
     * By default Spring turns the "scope" claim into SCOPE_ authorities. Our
     * tokens carry a "role" claim whose value already starts with "ROLE_", so
     * we point the converter at it and add no prefix of our own.
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName(JwtCookieService.ROLE_CLAIM);
        authorities.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    /**
     * Replaces the old WebMvcConfigurer-based CORS setup. allowCredentials is
     * required for the browser to send our cookie cross-origin, and it forbids
     * the "*" wildcard: the origin has to be spelled out.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigin));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
