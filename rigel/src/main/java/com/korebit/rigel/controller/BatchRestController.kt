package com.korebit.rigel.controller

import com.korebit.rigel.dto.BatchDto
import com.korebit.rigel.dto.request.BatchUpsertRequest
import com.korebit.rigel.service.BatchService
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
@RequestMapping("/batches")
class BatchRestController(
    private val batchService: BatchService,
) {

    @GetMapping("/product/{productName}")
    fun getByProduct(@PathVariable productName: String): ResponseEntity<List<BatchDto>> =
        ResponseEntity.ok(batchService.getBatchesByProduct(productName))

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<BatchDto> = ResponseEntity.ok(batchService.getBatchById(id))

    @PostMapping("/add")
    fun create(@RequestBody request: BatchUpsertRequest): ResponseEntity<BatchDto> =
        ResponseEntity.status(HttpStatus.CREATED).body(batchService.createBatch(request))

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: BatchUpsertRequest): ResponseEntity<BatchDto> =
        ResponseEntity.ok(batchService.updateBatch(id, request))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        batchService.deleteBatch(id)
        return ResponseEntity.noContent().build()
    }
}
