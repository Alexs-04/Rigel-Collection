package com.korebit.rigel.controller

import com.korebit.rigel.dto.SupplierDto
import com.korebit.rigel.service.SupplierService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/suppliers")
class SupplierRestController(
    private val supplierService: SupplierService
) {

    @GetMapping("/all")
    fun getAllSuppliers() = supplierService.getAllSuppliers()

    @GetMapping("/{supplierName}/products")
    fun getProductsBySupplier(@PathVariable supplierName: String) = supplierService.getProductsBySupplier(supplierName)

    @GetMapping("/{name}")
    fun findSupplierByName(@PathVariable name: String): SupplierDto = findSupplierByName(name)

    @PostMapping("/add")
    fun saveSupplier(@RequestBody supplier: SupplierDto): ResponseEntity<Any> =
        ResponseEntity.ok(supplierService.saveSupplier(supplier))
}