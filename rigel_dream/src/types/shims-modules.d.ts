import type {Context} from 'react'
import type {AxiosInstance} from 'axios'

declare module '*context/AuthContext' {
    export interface AuthUser {
        username?: string
        role?: string
        active?: boolean
    }

    export interface AuthContextValue {
        user?: AuthUser | null
        accessToken?: string | null
        hasRole?: (...roles: string[]) => boolean
        login?: (email: string, password: string) => Promise<{success: boolean}>
        logout?: () => void
    }

    export const AuthContext: Context<AuthContextValue | null>
}

declare module '*services/api' {
    const api: AxiosInstance
    export default api
}


