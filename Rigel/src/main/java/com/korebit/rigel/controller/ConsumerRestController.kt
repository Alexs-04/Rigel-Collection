package com.korebit.rigel.controller

import com.korebit.rigel.dto.ConsumerDto
import com.korebit.rigel.dto.UserAdminDto
import com.korebit.rigel.dto.request.UserStatusRequest
import com.korebit.rigel.dto.request.UserUpsertRequest
import com.korebit.rigel.dto.response.Response
import com.korebit.rigel.service.ConsumerService
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

@RestController
@RequestMapping("/consumer/api")
class ConsumerRestController(
    private val consumerService: ConsumerService,
) {
    @PostMapping("/add")
    fun registerConsumer(@RequestBody consumer : ConsumerDto) : Response {
        val aux = consumerService.saveConsumer(consumer)

        return Response(
            "Consumer registered successfully with token: ${aux.accessToken}",
            201,
            true
        )
    }

    @GetMapping("/all")
    fun getAllConsumers() : List<ConsumerDto> {
        return consumerService.getAllConsumers()
    }

    @PostMapping("/findByEmail")
    fun findByEmail(email: String) : ConsumerDto? {
        return consumerService.findByEmail(email)
    }

    @GetMapping("/findByUsername")
    fun findByUsername(username: String) : ConsumerDto? {
        return consumerService.findByUsername(username)
    }

    @GetMapping("/users")
    fun getUsers(@RequestParam(required = false) search: String?): List<UserAdminDto> {
        return consumerService.getUsers(search)
    }

    @PostMapping("/users")
    fun createUser(@RequestBody request: UserUpsertRequest): ResponseEntity<Response> {
        return ResponseEntity.status(201).body(consumerService.createUser(request))
    }

    @PutMapping("/users/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody request: UserUpsertRequest): ResponseEntity<Response> {
        return ResponseEntity.ok(consumerService.updateUser(id, request))
    }

    @PatchMapping("/users/{id}/status")
    fun updateUserStatus(
        @PathVariable id: Long,
        @RequestBody request: UserStatusRequest,
        authentication: Authentication,
    ): ResponseEntity<Response> {
        return ResponseEntity.ok(consumerService.updateUserStatus(id, request, authentication.name))
    }

}