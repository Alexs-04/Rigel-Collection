package com.korebit.rigel.controller

import com.korebit.rigel.dto.request.ProductAddRequest
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

    @PostMapping("/add")
    fun saveProduct(@RequestBody product: ProductAddRequest): ResponseEntity<Any> {
        return ResponseEntity.ok(productService.saveProduct(product))
    }

    @PutMapping("/{name}")
    fun updateProduct(@PathVariable name: String, @RequestBody product: ProductAddRequest): ResponseEntity<Any> {
        return ResponseEntity.ok(productService.updateProduct(name, product))
    }

    @DeleteMapping("/{name}")
    fun deleteProduct(@PathVariable name: String): ResponseEntity<Any> {
        return ResponseEntity.ok(productService.deleteProduct(name))
    }
}