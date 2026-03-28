import api from './api'
import type {FetchMovementsParams, SystemMovement, SystemMovementPage} from '../types/logs'

export async function fetchSystemMovements(params: FetchMovementsParams = {}): Promise<SystemMovementPage> {
    const response = await api.get<SystemMovementPage>('/logs', {params})
    return response.data
}

export async function fetchSystemMovementById(id: number): Promise<SystemMovement> {
    const response = await api.get<SystemMovement>(`/logs/${id}`)
    return response.data
}

