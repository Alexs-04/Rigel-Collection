export interface PurchaseDto {
    id: number
    code: string
    purchaseDate: string
    quantity: number
    unitPrice: number
    totalPrice: number
    notes: string
    productName: string
    supplierName: string
    batchId: number
    batchCode: string
}

export interface PurchaseFormValues {
    productName: string
    supplierName: string
    quantity: string
    unitPrice: string
    purchaseDate: string
    notes: string
    useExistingBatch: boolean
    batchId: string
    batchCode: string
    receptionDate: string
    expirationDate: string
    available: boolean
    batchPrice: string
}

