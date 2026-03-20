import type {SupplierDetail, SupplierFormValues} from '../../types/suppliers'

type SupplierField = keyof Pick<SupplierFormValues, 'name' | 'contactEmail' | 'phoneNumber' | 'address'>

interface SupplierFieldsGridProps {
    value: SupplierFormValues | SupplierDetail
    disabled: boolean
    isRequired?: boolean
    onChange: (key: SupplierField, value: string) => void
}

export default function SupplierFieldsGrid({value, disabled, isRequired = false, onChange}: SupplierFieldsGridProps) {
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
                placeholder="Correo"
                type="email"
                value={value.contactEmail || ''}
                disabled={disabled}
                required={isRequired}
                onChange={(e) => onChange('contactEmail', e.target.value)}
            />
            <input
                className="input"
                placeholder="Telefono"
                value={value.phoneNumber || ''}
                disabled={disabled}
                required={isRequired}
                onChange={(e) => onChange('phoneNumber', e.target.value)}
            />
            <input
                className="input"
                placeholder="Direccion"
                value={value.address || ''}
                disabled={disabled}
                required={isRequired}
                onChange={(e) => onChange('address', e.target.value)}
            />
        </div>
    )
}

