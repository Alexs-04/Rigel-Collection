package com.korebit.rigel.model.beans

import com.korebit.rigel.model.extra.ProductSupplier
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinColumns
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate

@Entity(name = "purchases")
@Table(
	name = "purchases",
	uniqueConstraints = [
		UniqueConstraint(name = "uk_purchases_code", columnNames = ["code"])
	]
)
class Purchase(
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	var id: Long? = null,

	@Column(nullable = false, length = 64)
	var code: String = "",

	@Column(nullable = false)
	var purchaseDate: LocalDate = LocalDate.now(),

	@Column(nullable = false)
	var quantity: Int = 0,

	@Column(nullable = false, precision = 19, scale = 4)
	var unitPrice: BigDecimal = BigDecimal.ZERO,

	@Column(nullable = false, precision = 19, scale = 4)
	var totalPrice: BigDecimal = BigDecimal.ZERO,

	@Column(nullable = false, length = 256)
	var notes: String = "",

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumns(
		value = [
			JoinColumn(name = "product_id", referencedColumnName = "product_id", nullable = false),
			JoinColumn(name = "supplier_id", referencedColumnName = "supplier_id", nullable = false)
		]
	)
	var productSupplier: ProductSupplier? = null,

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "batch_id", nullable = false)
	var batch: Batch? = null,
) : Serializable
