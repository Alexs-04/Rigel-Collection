package com.korebit.rigel.controller

import com.korebit.rigel.dto.request.InviteUserRequest
import com.korebit.rigel.dto.response.InviteUserResponse
import com.korebit.rigel.enums.Role
import com.korebit.rigel.service.email.UserInvitationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasAnyRole('ADMIN', 'ROOT')")
class AdminUserController(
    private val invitationService: UserInvitationService,
) {
    @PostMapping("/invite")
    fun inviteUser(@RequestBody request: InviteUserRequest): ResponseEntity<InviteUserResponse> {
        val consumer = invitationService.inviteUser(
            name = request.name,
            username = request.username,
            email = request.email,
            role = request.role ?: Role.USER,
            phoneNumber = request.phoneNumber ?: "",
        )
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(InviteUserResponse(
                id = consumer.id!!,
                email = consumer.email,
                message = "Invitación enviada a ${consumer.email}"
            ))
    }
}