import api from './api'
import type {ProductBatch, ProductSummary} from '../types/products'
import type {PosCatalogItem, PosTicket, PosTicketCreatePayload} from '../types/pos'

function isBatchSellable(batch: ProductBatch, todayIso: string): boolean {
    return batch.available && batch.remainingAmount > 0 && String(batch.expirationDate || '') >= todayIso
}

function calculateAvailableUnits(product: ProductSummary, todayIso: string): number {
    return (product.suppliers || []).reduce((sum, supplier) => {
        const supplierUnits = (supplier.batches || [])
            .filter((batch) => isBatchSellable(batch, todayIso))
            .reduce((acc, batch) => acc + Number(batch.remainingAmount || 0), 0)
        return sum + supplierUnits
    }, 0)
}

export function toPosCatalogItem(product: ProductSummary, todayIso: string): PosCatalogItem {
    return {
        name: String(product.name || ''),
        barcode: String(product.barcode || ''),
        description: String(product.description || ''),
        category: String(product.category || 'OTHERS'),
        price: Number(product.price || 0),
        imageUrl: String(product.imageUrl || ''),
        availableUnits: calculateAvailableUnits(product, todayIso),
    }
}

export async function fetchPosCatalog(): Promise<PosCatalogItem[]> {
    const todayIso = new Date().toISOString().slice(0, 10)
    const res = await api.get('/product/all')
    const products = Array.isArray(res.data) ? (res.data as ProductSummary[]) : []
    return products.map((product) => toPosCatalogItem(product, todayIso))
}

export async function fetchPosTickets(): Promise<PosTicket[]> {
    const res = await api.get('/tickets')
    return Array.isArray(res.data) ? (res.data as PosTicket[]) : []
}

export async function createPosTicket(payload: PosTicketCreatePayload): Promise<void> {
    await api.post('/tickets', payload)
}

