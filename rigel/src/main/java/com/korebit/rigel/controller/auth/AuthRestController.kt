package com.korebit.rigel.controller.auth

import com.korebit.rigel.dto.request.LoginRequest
import com.korebit.rigel.dto.request.RefreshTokenRequest
import com.korebit.rigel.dto.response.TokenResponse
import com.korebit.rigel.service.AuthService
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthRestController(
    private val authService: AuthService,
) {
    /** Validates credentials and returns a new access/refresh token pair. `401` on failure. */
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<TokenResponse> =
        ResponseEntity.ok(authService.login(request))

    /** Rotates the refresh token: returns a new pair and invalidates the one sent. `401` on failure. */
    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshTokenRequest): ResponseEntity<TokenResponse> =
        ResponseEntity.ok(authService.refresh(request.refreshToken))

    /** Revokes every session of the caller. Always `204`, even if the token was already invalid. */
    @PostMapping("/logout")
    fun logout(
        @RequestHeader(HttpHeaders.AUTHORIZATION, required = false) authorization: String?,
    ): ResponseEntity<Void> {
        authService.logout(authorization)
        return ResponseEntity.noContent().build()
    }
}
