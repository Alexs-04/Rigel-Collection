package com.korebit.rigel.controller

import com.korebit.rigel.dto.response.MessageResponse
import com.korebit.rigel.service.email.PasswordResetService
import com.korebit.rigel.service.email.UserInvitationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** Public account flows reached from email links. */
@RestController
@RequestMapping("/account")
class AccountRestController(
    private val invitationService: UserInvitationService,
    private val passwordResetService: PasswordResetService,
) {
    data class ActivateAccountRequest(val token: String, val password: String)
    data class ForgotPasswordRequest(val email: String)
    data class ResetPasswordRequest(val token: String, val password: String)

    @PostMapping("/activate")
    fun activateAccount(@RequestBody request: ActivateAccountRequest): ResponseEntity<MessageResponse> {
        invitationService.activateAccount(request.token, request.password)
        return ResponseEntity.ok(MessageResponse("Cuenta activada correctamente"))
    }

    /** Always `200`, so the endpoint cannot be used to discover registered emails. */
    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestBody request: ForgotPasswordRequest): ResponseEntity<MessageResponse> {
        passwordResetService.requestPasswordReset(request.email.trim())
        return ResponseEntity.ok(MessageResponse("Si el correo está registrado recibirás un enlace en breve"))
    }

    @PostMapping("/reset-password")
    fun resetPassword(@RequestBody request: ResetPasswordRequest): ResponseEntity<MessageResponse> {
        passwordResetService.resetPassword(request.token, request.password)
        return ResponseEntity.ok(MessageResponse("Contraseña actualizada correctamente"))
    }
}
