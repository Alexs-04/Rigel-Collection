package com.korebit.rigel.enums

/**
 * Kinds of tokens persisted in the `tokens` table.
 *
 * - [BEARER]: JWT access token (stored by its `jti`).
 * - [REFRESH]: JWT refresh token (stored by its `jti`).
 * - [EMAIL_VERIFICATION] / [PASSWORD_RESET]: one-time UUID links sent by email.
 */
enum class TokenType {
    BEARER,
    REFRESH,
    EMAIL_VERIFICATION,
    PASSWORD_RESET
}
