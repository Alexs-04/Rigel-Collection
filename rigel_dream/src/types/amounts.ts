export type AmountStatus = 'ACTIVE' | 'EXPIRED' | 'RETURNED' | 'BOUGHT_OUT'

export interface ContainerTypeOption {
    value: string
    label: string
    suggestedSalePrice: number
    suggestedBuyoutPrice: number
}

export interface AmountItem {
    folio: number
    description: string
    type: string
    typeLabel: string
    customerName: string
    quantity: number
    saleUnitPrice: number
    buyoutUnitPrice: number | null
    total: number
    buyoutTotal: number | null
    created: string
    expirationDate: string
    returned: boolean
    returnedAt: string | null
    boughtOutAt: string | null
    ownerId: number | null
    ownerUsername: string | null
    notes: string
    status: AmountStatus
}

export interface AmountCreatePayload {
    description: string
    type: string
    customerName: string
    quantity: number
    saleUnitPrice: number
    buyoutUnitPrice?: number | null
    expirationDate: string
    notes: string
}

export interface AmountUpdatePayload extends AmountCreatePayload {}

export interface AmountBuyoutPayload {
    buyoutUnitPrice: number
    notes?: string
}

export interface AmountFormValues {
    description: string
    type: string
    customerName: string
    quantity: string
    saleUnitPrice: string
    buyoutUnitPrice: string
    expirationDate: string
    notes: string
}

