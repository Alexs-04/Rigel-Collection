package com.korebit.rigel.controller.auth

import com.korebit.rigel.dto.LoginRequest
import com.korebit.rigel.dto.response.Response
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
    fun tryLogin(@RequestBody request: LoginRequest): ResponseEntity<Response> {
        authService.login(request)

        return ResponseEntity.ok(
            Response(
                "Success login",
                200,
                true
            )
        )
    }
}