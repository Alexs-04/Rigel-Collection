package com.korebit.rigel.controller

import com.korebit.rigel.dto.response.Response
import com.korebit.rigel.service.email.PasswordResetService
import com.korebit.rigel.service.email.UserInvitationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/account")
class AccountRestController(
    private val invitationService: UserInvitationService,
    private val passwordResetService: PasswordResetService,
) {

    @PostMapping("/activate")
    fun activateAccount(@RequestBody request: ActivateAccountRequest): ResponseEntity<Response> {
        invitationService.activateAccount(request.token, request.password)
        return ResponseEntity.ok(Response("Cuenta activada correctamente", 200, true))
    }

    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestBody request: ForgotPasswordRequest): ResponseEntity<Response> {
        // Always returns 200 — do not reveal whether the email exists
        passwordResetService.requestPasswordReset(request.email)
        return ResponseEntity.ok(Response("Si el correo está registrado recibirás un enlace en breve", 200, true))
    }

    @PostMapping("/reset-password")
    fun resetPassword(@RequestBody request: ResetPasswordRequest): ResponseEntity<Response> {
        passwordResetService.resetPassword(request.token, request.password)
        return ResponseEntity.ok(Response("Contraseña actualizada correctamente", 200, true))
    }

    data class ActivateAccountRequest(val token: String, val password: String)
    data class ForgotPasswordRequest(val email: String)
    data class ResetPasswordRequest(val token: String, val password: String)
}