import api from './api'

export async function fetchDashboardSnapshot({date, limit = 5} = {}) {
    const params = {}
    if (date) params.date = date
    if (limit) params.limit = limit

    const response = await api.get('/dashboard/snapshot', {params})
    return response.data
}

