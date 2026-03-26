export function formatCurrency(value: number | string | undefined): string {
    return new Intl.NumberFormat('es-MX', {
        style: 'currency',
        currency: 'MXN',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    }).format(Number(value || 0))
}

export function formatNumber(value: number | string | undefined): string {
    return new Intl.NumberFormat('es-MX').format(Number(value || 0))
}

export function percentage(value: number | string | undefined, total: number): number {
    if (!total) return 0
    return Math.max(0, Math.min(100, (Number(value || 0) / total) * 100))
}

