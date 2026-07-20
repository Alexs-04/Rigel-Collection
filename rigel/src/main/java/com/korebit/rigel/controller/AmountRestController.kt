package com.korebit.rigel.controller

import com.korebit.rigel.dto.request.AmountBuyoutRequest
import com.korebit.rigel.dto.request.AmountCreateRequest
import com.korebit.rigel.dto.request.AmountUpdateRequest
import com.korebit.rigel.service.AmountService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/amounts")
class AmountRestController(
    private val amountService: AmountService,
) {

    @GetMapping("/all")
    fun getAllAmounts() = amountService.getAllAmounts()

    @GetMapping("/types")
    fun getContainerTypes() = amountService.getContainerTypes()

    @GetMapping("/{folio}")
    fun getAmountByFolio(@PathVariable folio: Long) = amountService.getAmountByFolio(folio)

    @PostMapping("/add")
    fun createAmount(
        @RequestBody request: AmountCreateRequest,
        authentication: Authentication,
    ): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.CREATED).body(amountService.createAmount(request, authentication.name))
    }

    @PutMapping("/{folio}")
    fun updateAmount(@PathVariable folio: Long, @RequestBody request: AmountUpdateRequest): ResponseEntity<Any> {
        return ResponseEntity.ok(amountService.updateAmount(folio, request))
    }

    @PatchMapping("/{folio}/return")
    fun markAsReturned(@PathVariable folio: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(amountService.markAsReturned(folio))
    }

    @PatchMapping("/{folio}/buyout")
    fun markAsBoughtOut(@PathVariable folio: Long, @RequestBody request: AmountBuyoutRequest): ResponseEntity<Any> {
        return ResponseEntity.ok(amountService.markAsBoughtOut(folio, request))
    }

    @DeleteMapping("/{folio}")
    fun deleteAmount(@PathVariable folio: Long): ResponseEntity<Any> {
        return ResponseEntity.ok(amountService.deleteAmount(folio))
    }
}

