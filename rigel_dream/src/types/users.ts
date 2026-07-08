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

// Invite form — no password, the user sets it themselves on activation
export interface UserFormValues {
    name: string
    username: string
    email: string
    phoneNumber: string
    role: UserRole
}

export interface UserDetail extends UserSummary {
    // password is not shown or edited here; reset is done via email flow
}