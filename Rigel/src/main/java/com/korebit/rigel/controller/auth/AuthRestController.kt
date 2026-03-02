package com.korebit.rigel.controller.auth

import com.korebit.rigel.dto.request.LoginRequest
import com.korebit.rigel.dto.response.TokenResponse
import com.korebit.rigel.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthRestController(
    private val authService: AuthService
) {
    @PostMapping("/login")
    fun tryLogin(@RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        val tokens = authService.login(request)

        return ResponseEntity.ok(tokens)
    }
}