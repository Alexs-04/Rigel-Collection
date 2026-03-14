import type {FormEvent} from 'react'
import type {SupplierFormValues} from '../../types/suppliers'
import SupplierFieldsGrid from './SupplierFieldsGrid'

interface SupplierFormCardProps {
    isAdmin: boolean
    userRole?: string
    form: SupplierFormValues
    saving: boolean
    formMessage: string
    onChangeForm: (key: keyof SupplierFormValues, value: string) => void
    onSubmit: (event: FormEvent<HTMLFormElement>) => void
}

export default function SupplierFormCard({
    isAdmin,
    userRole,
    form,
    saving,
    formMessage,
    onChangeForm,
    onSubmit,
}: SupplierFormCardProps) {
    if (!isAdmin) {
        return (
            <section className="card" style={{padding: 16}}>
                <p className="text-muted" style={{margin: 0}}>
                    Tu rol ({userRole || 'USER'}) permite consulta. Solo administradores pueden agregar, editar o
                    eliminar proveedores.
                </p>
            </section>
        )
    }

    return (
        <section className="card" style={{padding: 20}}>
            <h2 style={{marginTop: 0, marginBottom: 12, fontSize: 18}}>Agregar proveedor</h2>
            <form onSubmit={onSubmit} style={{display: 'grid', gap: 12}}>
                <SupplierFieldsGrid value={form} disabled={false} isRequired={true} onChange={onChangeForm} />

                <div>
                    <button className="btn-primary" type="submit" disabled={saving}>
                        {saving ? 'Guardando...' : 'Agregar proveedor'}
                    </button>
                </div>
            </form>
            {formMessage && <p className="text-muted" style={{marginBottom: 0}}>{formMessage}</p>}
        </section>
    )
}


