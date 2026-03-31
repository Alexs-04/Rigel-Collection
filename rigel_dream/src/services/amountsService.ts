import api from './api'
import type {
    AmountBuyoutPayload,
    AmountCreatePayload,
    AmountItem,
    AmountUpdatePayload,
    ContainerTypeOption,
} from '../types/amounts'

export async function fetchAmounts(): Promise<AmountItem[]> {
    const response = await api.get<AmountItem[]>('/amounts/all')
    return Array.isArray(response.data) ? response.data : []
}

export async function fetchAmountTypes(): Promise<ContainerTypeOption[]> {
    const response = await api.get<ContainerTypeOption[]>('/amounts/types')
    return Array.isArray(response.data) ? response.data : []
}

export async function createAmount(payload: AmountCreatePayload): Promise<void> {
    await api.post('/amounts/add', payload)
}

export async function updateAmount(folio: number, payload: AmountUpdatePayload): Promise<void> {
    await api.put(`/amounts/${folio}`, payload)
}

export async function markAmountAsReturned(folio: number): Promise<void> {
    await api.patch(`/amounts/${folio}/return`)
}

export async function markAmountAsBoughtOut(folio: number, payload: AmountBuyoutPayload): Promise<void> {
    await api.patch(`/amounts/${folio}/buyout`, payload)
}

export async function deleteAmount(folio: number): Promise<void> {
    await api.delete(`/amounts/${folio}`)
}

