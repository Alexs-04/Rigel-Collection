import {roleOptions, type UserDetail, type UserFormValues, type UserRole} from '../../types/users'

type EditableUserField = keyof Pick<UserFormValues, 'name' | 'username' | 'email' | 'phoneNumber' | 'role' | 'password'>

interface UserFieldsGridProps {
    value: UserFormValues | UserDetail
    disabled: boolean
    isRequired?: boolean
    showPassword?: boolean
    passwordPlaceholder?: string
    onChange: (key: EditableUserField, value: string) => void
}

export default function UserFieldsGrid({
    value,
    disabled,
    isRequired = false,
    showPassword = false,
    passwordPlaceholder = 'Contrasena',
    onChange,
}: UserFieldsGridProps) {
    return (
        <div style={{display: 'grid', gap: 10, marginBottom: 14}}>
            <input
                className="input"
                placeholder="Nombre"
                value={value.name || ''}
                disabled={disabled}
                required={isRequired}
                onChange={(e) => onChange('name', e.target.value)}
            />
            <input
                className="input"
                placeholder="Usuario"
                value={value.username || ''}
                disabled={disabled}
                required={isRequired}
                onChange={(e) => onChange('username', e.target.value)}
            />
            <input
                className="input"
                placeholder="Correo"
                type="email"
                value={value.email || ''}
                disabled={disabled}
                required={isRequired}
                onChange={(e) => onChange('email', e.target.value)}
            />
            <input
                className="input"
                placeholder="Telefono"
                value={value.phoneNumber || ''}
                disabled={disabled}
                onChange={(e) => onChange('phoneNumber', e.target.value)}
            />
            <select
                className="input"
                value={(value.role || 'USER') as UserRole}
                disabled={disabled}
                onChange={(e) => onChange('role', e.target.value)}
            >
                {roleOptions.map((role) => (
                    <option key={role} value={role}>{role}</option>
                ))}
            </select>

            {showPassword && (
                <input
                    className="input"
                    placeholder={passwordPlaceholder}
                    type="password"
                    value={value.password || ''}
                    disabled={disabled}
                    required={isRequired}
                    onChange={(e) => onChange('password', e.target.value)}
                />
            )}
        </div>
    )
}

