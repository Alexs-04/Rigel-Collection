export interface DashboardStats {
    totalProducts: number
    totalSuppliers: number
    totalBatches: number
    totalTickets: number
}

export interface DashboardTopItem {
    name: string
    total: number
}

export interface DashboardSalesSummary {
    day: number | string
    month: number | string
    year: number | string
}

export interface DashboardSnapshot {
    referenceDate: string
    stats: DashboardStats
    sales: DashboardSalesSummary
    topProducts: DashboardTopItem[]
    topSuppliers: DashboardTopItem[]
}

