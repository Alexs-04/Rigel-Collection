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
            <section className="card" style={{padding: 16}}>
                <p className="text-muted" style={{margin: 0}}>
                    Tu rol ({userRole || 'USER'}) permite consulta. Solo administradores pueden agregar, editar o
                    eliminar productos.
                </p>
            </section>
        )
    }

    return (
        <section className="card" style={{padding: 20}}>
            <h2 style={{marginTop: 0, marginBottom: 12, fontSize: 18}}>Agregar producto</h2>
            <p className="text-muted" style={{marginTop: 0, marginBottom: 12}}>
                Registra solo los datos esenciales del catalogo. Las cantidades de inventario se registran desde Compras.
            </p>
            <form onSubmit={onSubmit} style={{display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: 12}}>
                <ProductFieldsGrid
                    value={form}
                    suppliers={suppliers}
                    disabled={false}
                    isRequired={true}
                    onChange={onChangeForm}
                />

                <div style={{gridColumn: '1 / -1'}}>
                    <button className="btn-primary" type="submit" disabled={saving || suppliers.length === 0}>
                        {saving ? 'Guardando...' : 'Agregar producto'}
                    </button>
                </div>
            </form>

            {suppliers.length === 0 && (
                <p className="text-muted" style={{marginBottom: 0}}>
                    Debes registrar al menos un proveedor para crear productos.
                </p>
            )}
            {formMessage && <p className="text-muted" style={{marginBottom: 0}}>{formMessage}</p>}
        </section>
    )
}

