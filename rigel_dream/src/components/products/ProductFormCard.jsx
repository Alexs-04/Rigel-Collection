import React from 'react'
import ProductFieldsGrid from './ProductFieldsGrid.jsx'

export default function ProductFormCard({
    isAdmin,
    userRole,
    form,
    suppliers,
    saving,
    formMessage,
    onChangeForm,
    onSubmit,
}) {
    if (!isAdmin) {
        return (
            <section className="ui-card p-4">
                <p className="m-0 text-sm text-slate-500">
                    Tu rol ({userRole || 'USER'}) permite consulta. Solo administradores pueden agregar, editar o
                    eliminar productos.
                </p>
            </section>
        )
    }

    return (
        <section className="ui-card p-5">
            <h2 className="mb-3 mt-0 text-lg font-semibold text-slate-900">Agregar producto</h2>
            <p className="mb-3 mt-0 text-sm text-slate-500">
                Registra solo los datos esenciales del catalogo. Las cantidades de inventario se registran desde Compras.
            </p>
            <form onSubmit={onSubmit} className="grid gap-3 sm:grid-cols-2">
                <ProductFieldsGrid
                    value={form}
                    suppliers={suppliers}
                    disabled={false}
                    isRequired={true}
                    onChange={onChangeForm}
                />

                <div className="sm:col-span-2">
                    <button className="ui-btn-primary" type="submit" disabled={saving || suppliers.length === 0}>
                        {saving ? 'Guardando...' : 'Agregar producto'}
                    </button>
                </div>
            </form>

            {suppliers.length === 0 && (
                <p className="mb-0 text-sm text-slate-500">
                    Debes registrar al menos un proveedor para crear productos.
                </p>
            )}
            {formMessage && <p className="mb-0 text-sm text-slate-500">{formMessage}</p>}
        </section>
    )
}

