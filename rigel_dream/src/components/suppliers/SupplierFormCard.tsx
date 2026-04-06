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
            <section className="ui-card p-4">
                <p className="m-0 text-sm text-slate-500">
                    Tu rol ({userRole || 'USER'}) permite consulta. Solo administradores pueden agregar, editar o
                    eliminar proveedores.
                </p>
            </section>
        )
    }

    return (
        <section className="ui-card p-5">
            <h2 className="mb-3 mt-0 text-lg font-semibold text-slate-900">Agregar proveedor</h2>
            <form onSubmit={onSubmit} className="grid gap-3">
                <SupplierFieldsGrid value={form} disabled={false} isRequired={true} onChange={onChangeForm} />

                <div>
                    <button className="ui-btn-primary" type="submit" disabled={saving}>
                        {saving ? 'Guardando...' : 'Agregar proveedor'}
                    </button>
                </div>
            </form>
            {formMessage && <p className="mb-0 text-sm text-slate-500">{formMessage}</p>}
        </section>
    )
}


