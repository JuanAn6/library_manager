package com.library.manager.validation;

import java.util.regex.Pattern;

/**
 * The one definition of what counts as an email address here, shared by the
 * registration form and the user edit screen so the two cannot drift apart.
 *
 * <p>Deliberately permissive: the only way to prove an address is real is to
 * send a message to it, so a strict pattern buys nothing and rejects valid
 * addresses. This catches typing mistakes, nothing more.
 */
public final class EmailRules {

    private static final Pattern PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    /** Matches the column width in the users table. */
    public static final int MAX_LENGTH = 180;

    private EmailRules() {}

    public static boolean isValid(String email) {
        return email != null && email.length() <= MAX_LENGTH && PATTERN.matcher(email).matches();
    }
}
