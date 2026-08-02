package com.korebit.rigel.controller

import com.korebit.rigel.dto.request.AddRelationRequest
import com.korebit.rigel.dto.request.ProductAddRequest
import com.korebit.rigel.dto.request.ProductBatchRequest
import com.korebit.rigel.dto.request.ImageUploadRequest
import com.korebit.rigel.dto.response.ImageUploadResponse
import com.korebit.rigel.dto.response.Response
import com.korebit.rigel.service.ProductService
import com.korebit.rigel.service.image.CloudinaryImageService
import org.springframework.http.ResponseEntity
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
@RequestMapping("/product")
class ProductRestController(
    private val productService: ProductService,
    private val cloudinaryImageService: CloudinaryImageService,
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

    @PostMapping("/upload-image")
    fun uploadProductImage(@RequestBody request: ImageUploadRequest): ResponseEntity<ImageUploadResponse> {
        if (request.imageBase64.isNullOrBlank()) {
            return ResponseEntity.badRequest().body(
                ImageUploadResponse(
                    success = false,
                    message = "Imagen en Base64 requerida"
                )
            )
        }

        if (request.fileName.isNullOrBlank()) {
            return ResponseEntity.badRequest().body(
                ImageUploadResponse(
                    success = false,
                    message = "Nombre de archivo requerido"
                )
            )
        }

        return try {
            val response = cloudinaryImageService.uploadImage(request.imageBase64, request.fileName)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(
                ImageUploadResponse(
                    success = false,
                    message = e.message ?: "Error al cargar imagen"
                )
            )
        }
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

    @PatchMapping("/{barcode}/min-stock")
    fun updateMinStock(
        @PathVariable barcode: String,
        @RequestBody body: Map<String, Int?>,
    ): ResponseEntity<Response> {
        val result = productService.updateMinStock(barcode, body["minStock"])
        return ResponseEntity.ok(result)
    }
}