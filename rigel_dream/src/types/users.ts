export type UserRole = 'ROOT' | 'ADMIN' | 'USER' | 'SUPPLIER'

export const roleOptions: UserRole[] = ['ROOT', 'ADMIN', 'USER', 'SUPPLIER']

export interface UserSummary {
    id: number
    name: string
    username: string
    email: string
    phoneNumber: string
    role: UserRole
    active: boolean
}

export interface UserFormValues {
    name: string
    username: string
    email: string
    phoneNumber: string
    role: UserRole
    password: string
}

export interface UserDetail extends UserSummary {
    password?: string
}

