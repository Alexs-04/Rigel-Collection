import api from './api'
import type {DashboardSnapshot} from '../types/dashboard'

interface FetchDashboardSnapshotParams {
    date?: string
    limit?: number
}

export async function fetchDashboardSnapshot({date, limit = 5}: FetchDashboardSnapshotParams = {}): Promise<DashboardSnapshot> {
    const params: Record<string, string | number> = {}
    if (date) params.date = date
    if (limit) params.limit = limit

    const response = await api.get<DashboardSnapshot>('/dashboard/snapshot', {params})
    return response.data
}

