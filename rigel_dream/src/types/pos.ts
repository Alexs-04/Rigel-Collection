export interface PosCatalogItem {
    name: string
    barcode: string
    description: string
    category: string
    price: number
    imageUrl: string
    availableUnits: number
}

export interface PosCartItem {
    barcode: string
    productName: string
    quantity: number
    unitPrice: number
    discount: number
}

export interface PosTicketDetail {
    barcode: string
    quantity: number
    price: number
    discount: number
    batchCode?: string | null
    productName: string
}

export interface PosTicket {
    consumer: string
    barcode: string
    description: string
    dateAndTime: string
    totalAmount: number
    products: PosTicketDetail[]
}

export interface PosTicketCreatePayload {
    description: string
    dateAndTime: string
    products: Array<{
        barcode: string
        quantity: number
        price: number
        discount: number
    }>
    currentConsumerEmail: string
}

