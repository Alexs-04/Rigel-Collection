export type ProductCategory = string

export interface BatchSummary {
    id: number
    code: string
    receptionDate: string
    expirationDate: string
    receivedAmount: number
    remainingAmount: number
    available: boolean
    price: number
    notes?: string
}

export interface SupplierSummary {
    name: string
    supplyPrice: number
    batches: BatchSummary[]
}

export interface ProductSummary {
    name: string
    description: string
    barcode: string
    category: string
    price: number
    stock: number
    imageUrl?: string
    cloudinaryPublicId?: string
    suppliers: SupplierSummary[]
    perishable?: boolean
    minStock?: number        // null = sin umbral configurado
}

/** Derived: true when minStock is set and current stock is at or below it */
export function isLowStock(product: ProductSummary): boolean {
    return product.minStock != null && product.stock <= product.minStock
}

/** Derived: true when minStock is set and stock is at zero */
export function isOutOfStock(product: ProductSummary): boolean {
    return product.stock <= 0
}