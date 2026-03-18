package com.korebit.rigel.controller

import com.korebit.rigel.dto.request.PurchaseCreateRequest
import com.korebit.rigel.service.PurchaseService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/purchases")
class PurchaseRestController(
    private val purchaseService: PurchaseService,
) {

    @GetMapping("/all")
    fun getAllPurchases() = purchaseService.getAllPurchases()

    @GetMapping("/{id}")
    fun getPurchaseById(@PathVariable id: Long) = purchaseService.getPurchaseById(id)

    @GetMapping("/product/{productName}")
    fun getPurchasesByProduct(@PathVariable productName: String) = purchaseService.getPurchasesByProduct(productName)

    @GetMapping("/supplier/{supplierName}")
    fun getPurchasesBySupplier(@PathVariable supplierName: String) = purchaseService.getPurchasesBySupplier(supplierName)

    @PostMapping("/add")
    fun createPurchase(@RequestBody request: PurchaseCreateRequest): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseService.createPurchase(request))
    }
}

