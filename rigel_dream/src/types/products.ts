export interface ProductBatch {
    id: number
    code: string
    receptionDate: string
    expirationDate: string
    receivedAmount: number
    remainingAmount: number
    available: boolean
    price: number
    notes: string
}

export interface ProductSupplier {
    name: string
    supplyPrice: number
    batches: ProductBatch[]
}

export interface ProductSummary {
    name: string
    description: string
    barcode: string
    category: string
    price: number
    stock: number
    imageUrl: string
    suppliers: ProductSupplier[]
}

