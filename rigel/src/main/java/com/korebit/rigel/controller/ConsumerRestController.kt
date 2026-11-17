package com.korebit.rigel.controller

import com.korebit.rigel.dto.UserAdminDto
import com.korebit.rigel.dto.request.UserStatusRequest
import com.korebit.rigel.dto.request.UserUpsertRequest
import com.korebit.rigel.service.ConsumerService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * User administration. Every endpoint requires ROOT/ADMIN, and the `users` endpoints require ROOT
 * (see `SecurityConfig`). There is no public registration: accounts are created here or
 * through `/admin/users/invite`.
 */
@RestController
@RequestMapping("/consumer/api")
class ConsumerRestController(
    private val consumerService: ConsumerService,
) {
    @GetMapping("/all")
    fun getAllConsumers(): ResponseEntity<List<UserAdminDto>> = ResponseEntity.ok(consumerService.getUsers())

    @GetMapping("/findByEmail")
    fun findByEmail(@RequestParam email: String): ResponseEntity<UserAdminDto> =
        ResponseEntity.ok(consumerService.findByEmail(email))

    @GetMapping("/findByUsername")
    fun findByUsername(@RequestParam username: String): ResponseEntity<UserAdminDto> =
        ResponseEntity.ok(consumerService.findByUsername(username))

    @GetMapping("/users")
    fun getUsers(@RequestParam(required = false) search: String?): ResponseEntity<List<UserAdminDto>> =
        ResponseEntity.ok(consumerService.getUsers(search))

    @PostMapping("/users")
    fun createUser(@RequestBody request: UserUpsertRequest): ResponseEntity<UserAdminDto> =
        ResponseEntity.status(HttpStatus.CREATED).body(consumerService.createUser(request))

    @PutMapping("/users/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody request: UserUpsertRequest): ResponseEntity<UserAdminDto> =
        ResponseEntity.ok(consumerService.updateUser(id, request))

    @PatchMapping("/users/{id}/status")
    fun updateUserStatus(
        @PathVariable id: Long,
        @RequestBody request: UserStatusRequest,
        authentication: Authentication,
    ): ResponseEntity<UserAdminDto> =
        ResponseEntity.ok(consumerService.updateUserStatus(id, request, authentication.name))
}
