package com.korebit.rigel.controller

import com.korebit.rigel.dto.SystemMovementDto
import com.korebit.rigel.dto.SystemMovementPageDto
import com.korebit.rigel.service.SystemMovementService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/logs")
class SystemMovementRestController(
    private val systemMovementService: SystemMovementService,
) {

    @GetMapping
    fun getMovements(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) method: String?,
        @RequestParam(required = false) status: Int?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        fromDate: LocalDate?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        toDate: LocalDate?,
        @RequestParam(required = false, defaultValue = "true") importantOnly: Boolean,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
    ): SystemMovementPageDto {
        return systemMovementService.getMovements(search, method, status, fromDate, toDate, importantOnly, page, size)
    }

    @GetMapping("/{id}")
    fun getMovementById(@PathVariable id: Long): SystemMovementDto {
        return systemMovementService.getMovementById(id)
    }
}

