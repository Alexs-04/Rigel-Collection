import type {AxiosInstance} from 'axios'

declare const api: AxiosInstance

export const ACCESS_TOKEN_KEY: string
export const REFRESH_TOKEN_KEY: string
export const AUTH_INVALIDATED_EVENT: string
export const AUTH_REFRESHED_EVENT: string

export function getAccessToken(): string | null
export function getRefreshToken(): string | null
export function storeTokens(accessToken: string, refreshToken: string): void
export function clearTokens(): void
export function refreshAccessToken(): Promise<string>
export function logoutRequest(): Promise<void>

export default api
