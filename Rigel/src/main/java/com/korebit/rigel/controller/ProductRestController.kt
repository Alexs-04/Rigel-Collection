package com.korebit.rigel.controller

import com.korebit.rigel.dto.request.AddRelationRequest
import com.korebit.rigel.dto.request.ProductAddRequest
import com.korebit.rigel.dto.request.ProductBatchRequest
import com.korebit.rigel.service.ProductService
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
@RequestMapping("/product")
class ProductRestController(
    private val productService: ProductService
) {
    @GetMapping("/all")
    fun getAllProducts() = productService.getAllProducts()

    @GetMapping("/{name}")
    fun getProductByName(@PathVariable name: String) = productService.findProductByName(name)

    @GetMapping("/{name}/batches")
    fun getProductBatches(@PathVariable name: String) = productService.getBatchesByProduct(name)

    @PostMapping("/add")
    fun saveProduct(@RequestBody product: ProductAddRequest): ResponseEntity<Any> {
        return ResponseEntity.ok(productService.saveProduct(product))
    }

    @PutMapping("/{name}")
    fun updateProduct(@PathVariable name: String, @RequestBody product: ProductAddRequest): ResponseEntity<Any> {
        return ResponseEntity.ok(productService.updateProduct(name, product))
    }

    @PutMapping("/{name}/batch")
    fun addBatchToProduct(@PathVariable name: String, @RequestBody batch: ProductBatchRequest): ResponseEntity<Any> {
        return ResponseEntity.ok(productService.addBatchToProduct(name, batch))
    }

    @DeleteMapping("/{name}")
    fun deleteProduct(@PathVariable name: String): ResponseEntity<Any> {
        return ResponseEntity.ok(productService.deleteProduct(name))
    }

    @PutMapping("/add-relation")
    fun addSupplierToProduct(@RequestBody relation : AddRelationRequest): ResponseEntity<Any> {
        return ResponseEntity.ok(productService.addRelationToProduct(relation))
    }

    @DeleteMapping("/{productName}/relation/{supplierName}")
    fun removeSupplierFromProduct(
        @PathVariable productName: String,
        @PathVariable supplierName: String,
    ): ResponseEntity<Any> {
        return ResponseEntity.ok(productService.removeRelationFromProduct(productName, supplierName))
    }
}