export interface SystemMovement {
    id: number
    occurredAt: string
    username: string
    role: string
    method: string
    path: string
    status: number
    durationMs: number
    correlationId: string
}

export interface SystemMovementPage {
    items: SystemMovement[]
    page: number
    size: number
    totalElements: number
    totalPages: number
}

export interface FetchMovementsParams {
    search?: string
    method?: string
    status?: number
    fromDate?: string
    toDate?: string
    importantOnly?: boolean
    page?: number
    size?: number
}

