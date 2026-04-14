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
                <p className="ui-muted m-0 text-sm">
                    Tu rol ({userRole || 'USER'}) permite consulta. Solo administradores pueden agregar, editar o
                    eliminar productos.
                </p>
            </section>
        )
    }

    return (
        <section className="ui-card p-5">
            <h2 className="ui-title mb-3 mt-0 text-lg font-semibold">Agregar producto</h2>
            <p className="ui-muted mb-3 mt-0 text-sm">
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
                <p className="ui-muted mb-0 text-sm">
                    Debes registrar al menos un proveedor para crear productos.
                </p>
            )}
            {formMessage && <p className="ui-muted mb-0 text-sm">{formMessage}</p>}
        </section>
    )
}

