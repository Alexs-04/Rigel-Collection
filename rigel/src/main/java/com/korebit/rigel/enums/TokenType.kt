package com.korebit.rigel.enums;

/**
 * Kinds of tokens persisted in the {@code tokens} table.
 *
 * <ul>
 *   <li>{@link #BEARER}: JWT access token (stored by its {@code jti}).</li>
 *   <li>{@link #REFRESH}: JWT refresh token (stored by its {@code jti}).</li>
 *   <li>{@link #EMAIL_VERIFICATION} / {@link #PASSWORD_RESET}: one-time UUID links sent by email.</li>
 * </ul>
 */
public enum TokenType {
    BEARER,
    REFRESH,
    EMAIL_VERIFICATION,
    PASSWORD_RESET
}
