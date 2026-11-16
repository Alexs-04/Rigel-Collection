package com.korebit.rigel.controller

import com.korebit.rigel.dto.BatchDto
import com.korebit.rigel.dto.ProductDto
import com.korebit.rigel.dto.request.AddRelationRequest
import com.korebit.rigel.dto.request.ImageUploadRequest
import com.korebit.rigel.dto.request.ProductAddRequest
import com.korebit.rigel.dto.request.ProductBatchRequest
import com.korebit.rigel.dto.response.ImageUploadResponse
import com.korebit.rigel.service.ProductService
import com.korebit.rigel.service.image.CloudinaryImageService
import org.springframework.http.HttpStatus
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
    data class MinStockRequest(val minStock: Int? = null)

    @GetMapping("/all")
    fun getAllProducts(): ResponseEntity<List<ProductDto>> = ResponseEntity.ok(productService.getAllProducts())

    @GetMapping("/{name}")
    fun getProductByName(@PathVariable name: String): ResponseEntity<ProductDto> =
        ResponseEntity.ok(productService.findProductByName(name))

    @GetMapping("/{name}/batches")
    fun getProductBatches(@PathVariable name: String): ResponseEntity<List<BatchDto>> =
        ResponseEntity.ok(productService.getBatchesByProduct(name))

    @PostMapping("/add")
    fun saveProduct(@RequestBody product: ProductAddRequest): ResponseEntity<ProductDto> =
        ResponseEntity.status(HttpStatus.CREATED).body(productService.saveProduct(product))

    /**
     * Keeps the `{success, message}` body because the frontend relies on it to show upload errors.
     */
    @PostMapping("/upload-image")
    fun uploadProductImage(@RequestBody request: ImageUploadRequest): ResponseEntity<ImageUploadResponse> {
        val imageBase64 = request.imageBase64
        val fileName = request.fileName
        if (imageBase64.isNullOrBlank()) {
            return ResponseEntity.badRequest().body(ImageUploadResponse(success = false, message = "Imagen en Base64 requerida"))
        }
        if (fileName.isNullOrBlank()) {
            return ResponseEntity.badRequest().body(ImageUploadResponse(success = false, message = "Nombre de archivo requerido"))
        }

        return try {
            ResponseEntity.ok(cloudinaryImageService.uploadImage(imageBase64, fileName))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ImageUploadResponse(success = false, message = e.message ?: "Error al cargar imagen")
            )
        }
    }

    @PutMapping("/{name}")
    fun updateProduct(@PathVariable name: String, @RequestBody product: ProductAddRequest): ResponseEntity<ProductDto> =
        ResponseEntity.ok(productService.updateProduct(name, product))

    @PutMapping("/{name}/batch")
    fun addBatchToProduct(@PathVariable name: String, @RequestBody batch: ProductBatchRequest): ResponseEntity<ProductDto> =
        ResponseEntity.ok(productService.addBatchToProduct(name, batch))

    @DeleteMapping("/{name}")
    fun deleteProduct(@PathVariable name: String): ResponseEntity<Void> {
        productService.deleteProduct(name)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/add-relation")
    fun addSupplierToProduct(@RequestBody relation: AddRelationRequest): ResponseEntity<ProductDto> =
        ResponseEntity.ok(productService.addRelationToProduct(relation))

    @DeleteMapping("/{productName}/relation/{supplierName}")
    fun removeSupplierFromProduct(
        @PathVariable productName: String,
        @PathVariable supplierName: String,
    ): ResponseEntity<ProductDto> =
        ResponseEntity.ok(productService.removeRelationFromProduct(productName, supplierName))

    @PatchMapping("/{barcode}/min-stock")
    fun updateMinStock(
        @PathVariable barcode: String,
        @RequestBody body: MinStockRequest,
    ): ResponseEntity<ProductDto> = ResponseEntity.ok(productService.updateMinStock(barcode, body.minStock))
}
