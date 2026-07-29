package com.library.manager.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Tells the resource server where to find the token.
 *
 * <p>By default Spring reads it from the "Authorization: Bearer ..." header.
 * Our token lives in an HttpOnly cookie instead, so we swap only this step:
 * everything downstream (signature check, expiry check, building the
 * Authentication) stays exactly as Spring implements it.
 */
@Component
public class CookieBearerTokenResolver implements BearerTokenResolver {

    @Override
    public String resolve(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> JwtCookieService.COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> !value.isBlank())
                .findFirst()
                // null means "no token here", which the filter reads as an
                // anonymous request rather than as a failed authentication.
                .orElse(null);
    }
}
