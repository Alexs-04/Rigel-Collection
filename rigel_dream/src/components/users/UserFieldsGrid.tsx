import {roleOptions, type UserDetail, type UserFormValues, type UserRole} from '../../types/users'

interface UserFieldsGridProps {
    value: (UserFormValues | UserDetail) & {password?: string}
    disabled: boolean
    isRequired?: boolean
    showPassword?: boolean
    passwordPlaceholder?: string
    onChange: (key: any, value: string) => void
}

export default function UserFieldsGrid({
    value,
    disabled,
    isRequired = false,
    showPassword = false,
    passwordPlaceholder = 'Contraseña',
    onChange,
}: UserFieldsGridProps) {
    return (
        <div className="mb-3.5 grid gap-2.5">
            <input
                className="ui-input"
                placeholder="Nombre"
                value={value.name || ''}
                disabled={disabled}
                required={isRequired}
                onChange={(e) => onChange('name', e.target.value)}
            />
            <input
                className="ui-input"
                placeholder="Usuario"
                value={value.username || ''}
                disabled={disabled}
                required={isRequired}
                onChange={(e) => onChange('username', e.target.value)}
            />
            <input
                className="ui-input"
                placeholder="Correo"
                type="email"
                value={value.email || ''}
                disabled={disabled}
                required={isRequired}
                onChange={(e) => onChange('email', e.target.value)}
            />
            <input
                className="ui-input"
                placeholder="Telefono"
                value={value.phoneNumber || ''}
                disabled={disabled}
                onChange={(e) => onChange('phoneNumber', e.target.value)}
            />
            <select
                className="ui-input"
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
                    className="ui-input"
                    placeholder={passwordPlaceholder}
                    type="password"
                    value={(value as any).password || ''}
                    disabled={disabled}
                    required={isRequired}
                    onChange={(e) => onChange('password', e.target.value)}
                />
            )}
        </div>
    )
}

