import api from './api'
import type {ProductSummary} from '../types/products'

// @ts-ignore
const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080'
const ABSOLUTE_URL_PATTERN = /^(https?:)?\/\//i

export function resolveInventoryImageUrl(imageUrl: string | null | undefined): string {
    const normalized = String(imageUrl || '').trim()
    if (!normalized) return ''
    if (ABSOLUTE_URL_PATTERN.test(normalized) || normalized.startsWith('data:') || normalized.startsWith('blob:')) {
        return normalized
    }
    return normalized.startsWith('/') ? `${API_BASE}${normalized}` : `${API_BASE}/${normalized}`
}

export async function fetchInventoryProducts(): Promise<ProductSummary[]> {
    const res = await api.get('/product/all')
    const products = Array.isArray(res.data) ? (res.data as ProductSummary[]) : []

    return products
        .map((product) => ({
            ...product,
            name: String(product.name || ''),
            description: String(product.description || ''),
            barcode: String(product.barcode || ''),
            category: String(product.category || 'OTHERS'),
            price: Number(product.price || 0),
            stock: Number(product.stock || 0),
            imageUrl: resolveInventoryImageUrl(product.imageUrl),
            suppliers: Array.isArray(product.suppliers) ? product.suppliers : [],
            minStock: product.minStock != null ? Number(product.minStock) : undefined,
        }))
        .sort((a, b) => a.name.localeCompare(b.name, 'es', {sensitivity: 'base'}))
}

/**
 * Updates the low-stock threshold for a single product.
 * Pass null to remove the threshold.
 */
export async function updateProductMinStock(barcode: string, minStock: number | null): Promise<void> {
    await api.patch(`/product/${barcode}/min-stock`, {minStock})
}