package com.korebit.rigel.controller

import com.korebit.rigel.dto.ConsumerRequest
import com.korebit.rigel.dto.response.Response
import com.korebit.rigel.service.ConsumerService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/consumer/api")
class ConsumerRestController(
    private val consumerService: ConsumerService,
) {
    @PostMapping("/add")
    fun registerConsumer(@RequestBody consumer : ConsumerRequest) : Response {
        val aux = consumerService.saveConsumer(consumer)

        return Response(
            "Consumer registered successfully with token: ${aux.accessToken}",
            201,
            true
        )
    }
}