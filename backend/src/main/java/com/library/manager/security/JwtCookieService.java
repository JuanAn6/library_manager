package com.library.manager.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Issues the signed JWT and wraps it in the cookie the browser stores.
 *
 * <p>The token travels in an HttpOnly cookie rather than in a header kept in
 * localStorage: HttpOnly means no JavaScript on the page can read it, so an XSS
 * cannot steal the session. The price is that cookies are attached by the
 * browser automatically, which is what CSRF abuses, so the cookie is also
 * SameSite=Strict: the browser then refuses to send it on any request started
 * by another site.
 */
@Service
public class JwtCookieService {

    public static final String COOKIE_NAME = "access_token";

    /** Name of the JWT claim holding the role; SecurityConfig reads the same. */
    public static final String ROLE_CLAIM = "role";

    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtEncoder jwtEncoder;
    private final Duration ttl;
    private final boolean secureCookie;

    public JwtCookieService(JwtEncoder jwtEncoder,
                            @Value("${app.jwt.ttl}") Duration ttl,
                            @Value("${app.auth.cookie.secure}") boolean secureCookie) {
        this.jwtEncoder = jwtEncoder;
        this.ttl = ttl;
        this.secureCookie = secureCookie;
    }

    /** Builds a signed token for an already authenticated user. */
    public String issueToken(Authentication authentication) {
        Instant now = Instant.now();

        // A JWT is SIGNED, not encrypted: anyone can base64-decode it and read
        // these claims. Never put anything secret in here.
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("library-manager")
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .subject(authentication.getName())
                .claim(ROLE_CLAIM, grantedRole(authentication))
                .build();

        // The header must name the algorithm explicitly: the encoder defaults
        // to RS256 and would find no matching key in our HMAC secret.
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    /** The cookie that carries the token, set on a successful login. */
    public ResponseCookie buildCookie(String token) {
        return baseCookie(token).maxAge(ttl).build();
    }

    /** Same cookie with maxAge 0, which is how a cookie is deleted. */
    public ResponseCookie buildExpiredCookie() {
        return baseCookie("").maxAge(0).build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)          // unreadable from JavaScript
                .secure(secureCookie)    // HTTPS only; false in local dev
                .sameSite("Strict")      // never sent on cross-site requests
                .path("/");
    }

    /**
     * The role as the token carries it, prefix included. Spring also grants
     * bookkeeping authorities such as FACTOR_PASSWORD or FACTOR_BEARER, which
     * describe HOW the user authenticated; those are not roles and must not end
     * up in the token, or the next request would grant them back to itself.
     */
    private String grantedRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(ROLE_PREFIX))
                .findFirst()
                // Every user has a role in the database (the column is NOT
                // NULL), so this only guards against a token we did not issue.
                .orElseThrow(() -> new IllegalStateException("Authenticated user without a role"));
    }

    /** The same role without the prefix, which is what the frontend shows. */
    public String roleOf(Authentication authentication) {
        return grantedRole(authentication).substring(ROLE_PREFIX.length());
    }
}
