package com.korebit.rigel.controller

import com.korebit.rigel.dto.ProductDto
import com.korebit.rigel.dto.SupplierDto
import com.korebit.rigel.service.SupplierService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/suppliers")
class SupplierRestController(
    private val supplierService: SupplierService
) {

    @GetMapping("/all")
    fun getAllSuppliers(): ResponseEntity<List<SupplierDto>> = ResponseEntity.ok(supplierService.getAllSuppliers())

    @GetMapping("/{supplierName}/products")
    fun getProductsBySupplier(@PathVariable supplierName: String): ResponseEntity<List<ProductDto>> =
        ResponseEntity.ok(supplierService.getProductsBySupplier(supplierName))

    @GetMapping("/{name}")
    fun findSupplierByName(@PathVariable name: String): ResponseEntity<SupplierDto> =
        ResponseEntity.ok(supplierService.findSupplierByName(name))

    @PostMapping("/add")
    fun saveSupplier(@RequestBody supplier: SupplierDto): ResponseEntity<SupplierDto> =
        ResponseEntity.status(HttpStatus.CREATED).body(supplierService.saveSupplier(supplier))

    @PutMapping("/{name}")
    fun updateSupplier(@PathVariable name: String, @RequestBody supplier: SupplierDto): ResponseEntity<SupplierDto> =
        ResponseEntity.ok(supplierService.updateSupplier(name, supplier))

    @DeleteMapping("/{name}")
    fun deleteSupplier(@PathVariable name: String): ResponseEntity<Void> {
        supplierService.deleteSupplier(name)
        return ResponseEntity.noContent().build()
    }
}
