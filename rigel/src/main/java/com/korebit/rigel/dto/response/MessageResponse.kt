package com.korebit.rigel.dto.response

/**
 * Body for operations whose only meaningful result is a human readable confirmation.
 *
 * The HTTP status travels in the [org.springframework.http.ResponseEntity], never in the body.
 */
data class MessageResponse(val message: String)
