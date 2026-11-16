import {createContext} from 'react'

export interface AuthUser {
    /** Email (JWT subject) */
    username?: string
    name?: string
    role?: string
    active?: boolean
}

export interface AuthContextValue {
    user?: AuthUser | null
    accessToken?: string | null
    hasRole?: (...roles: string[]) => boolean
    login?: (email: string, password: string) => Promise<{success: boolean}>
    logout?: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | null>(null)

