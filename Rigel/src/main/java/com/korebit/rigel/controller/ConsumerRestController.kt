package com.korebit.rigel.controller

import com.korebit.rigel.request.ConsumerRequest
import com.korebit.rigel.service.ConsumerService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/consumers")
class ConsumerRestController(
    private val consumerService: ConsumerService) {

    @PostMapping("/add")
    fun createConsumer(@RequestBody consumerRequest: ConsumerRequest) {

    }
}