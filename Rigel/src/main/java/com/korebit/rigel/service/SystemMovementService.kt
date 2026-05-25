package com.korebit.rigel.service

import com.korebit.rigel.dto.SystemMovementDto
import com.korebit.rigel.dto.SystemMovementPageDto
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.SystemMovement
import com.korebit.rigel.repository.SystemMovementRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Service
/**
 * Persists and queries system movement audit records.
 */
class SystemMovementService(
    private val systemMovementRepository: SystemMovementRepository,
) {

    @Transactional
    /**
     * Persists a single movement event.
     *
     * @param username actor username.
     * @param role actor role.
     * @param method HTTP method.
     * @param path request path.
     * @param status HTTP status code.
     * @param durationMs request duration in milliseconds.
     * @param correlationId request correlation id.
     */
    fun recordMovement(
        username: String,
        role: String,
        method: String,
        path: String,
        status: Int,
        durationMs: Long,
        correlationId: String,
    ) {
        val movement = SystemMovement(
            occurredAt = Instant.now(),
            username = username,
            role = role,
            method = method,
            path = path,
            status = status,
            durationMs = durationMs,
            correlationId = correlationId,
        )
        systemMovementRepository.save(movement)
    }

    @Transactional(readOnly = true)
    /**
     * Retrieves paginated movement records with optional filters.
     *
     * @param search optional free-text search.
     * @param method optional HTTP method filter.
     * @param status optional HTTP status filter.
     * @param fromDate optional inclusive start date.
     * @param toDate optional inclusive end date.
     * @param importantOnly when true, keeps mutating and failed calls only.
     * @param page zero-based page number.
     * @param size page size.
     * @return paginated movement response.
     */
    fun getMovements(
        search: String?,
        method: String?,
        status: Int?,
        fromDate: LocalDate?,
        toDate: LocalDate?,
        importantOnly: Boolean,
        page: Int,
        size: Int,
    ): SystemMovementPageDto {
        val safePage = page.coerceAtLeast(0)
        val safeSize = size.coerceIn(1, 200)
        val pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "occurredAt"))

        val zone = ZoneId.systemDefault()
        val fromInstant = fromDate?.atStartOfDay(zone)?.toInstant()
        val toInstant = toDate?.plusDays(1)?.atStartOfDay(zone)?.toInstant()

        val normalizedSearch = search?.trim().orEmpty()
        val normalizedMethod = method?.trim().orEmpty()

        val spec = Specification<SystemMovement> { root, _, cb ->
            val predicates = mutableListOf<jakarta.persistence.criteria.Predicate>()

            if (normalizedSearch.isNotEmpty()) {
                val pattern = "%${normalizedSearch.lowercase()}%"
                val usernameLike = cb.like(cb.lower(root.get("username")), pattern)
                val pathLike = cb.like(cb.lower(root.get("path")), pattern)
                val correlationLike = cb.like(cb.lower(root.get("correlationId")), pattern)
                predicates += cb.or(usernameLike, pathLike, correlationLike)
            }

            if (normalizedMethod.isNotEmpty()) {
                predicates += cb.equal(cb.upper(root.get("method")), normalizedMethod.uppercase())
            }

            if (status != null) {
                predicates += cb.equal(root.get<Int>("status"), status)
            }

            if (fromInstant != null) {
                predicates += cb.greaterThanOrEqualTo(root.get("occurredAt"), fromInstant)
            }

            if (toInstant != null) {
                predicates += cb.lessThan(root.get("occurredAt"), toInstant)
            }

            if (importantOnly) {
                val mutatingMethods = root.get<String>("method").`in`("POST", "PUT", "PATCH", "DELETE")
                val failedStatus = cb.greaterThanOrEqualTo(root.get<Int>("status"), 400)
                val nonLogsPath = cb.notLike(root.get<String>("path"), "/logs%")
                predicates += cb.and(nonLogsPath, cb.or(mutatingMethods, failedStatus))
            }

            cb.and(*predicates.toTypedArray())
        }

        val pageResult = systemMovementRepository.findAll(spec, pageable)

        return SystemMovementPageDto(
            items = pageResult.content.map(SystemMovementDto::fromEntity),
            page = pageResult.number,
            size = pageResult.size,
            totalElements = pageResult.totalElements,
            totalPages = pageResult.totalPages,
        )
    }

    @Transactional(readOnly = true)
    /**
     * Retrieves a movement by id.
     *
     * @param id movement id.
     * @return movement DTO.
     */
    fun getMovementById(id: Long): SystemMovementDto {
        val movement = systemMovementRepository.findById(id).orElseThrow {
            EntityNotFundException("Movement with id $id not found")
        }
        return SystemMovementDto.fromEntity(movement)
    }
}

