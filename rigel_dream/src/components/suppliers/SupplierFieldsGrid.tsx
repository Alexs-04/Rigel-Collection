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
                placeholder="Correo"
                type="email"
                value={value.contactEmail || ''}
                disabled={disabled}
                required={isRequired}
                onChange={(e) => onChange('contactEmail', e.target.value)}
            />
            <input
                className="ui-input"
                placeholder="Telefono"
                value={value.phoneNumber || ''}
                disabled={disabled}
                required={isRequired}
                onChange={(e) => onChange('phoneNumber', e.target.value)}
            />
            <input
                className="ui-input"
                placeholder="Direccion"
                value={value.address || ''}
                disabled={disabled}
                required={isRequired}
                onChange={(e) => onChange('address', e.target.value)}
            />
        </div>
    )
}

