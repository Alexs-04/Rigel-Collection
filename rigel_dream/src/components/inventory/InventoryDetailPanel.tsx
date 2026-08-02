import {useState} from 'react'
import {formatPriceMxn, toCategoryLabel} from '../../utils/productPresentation'
import {isLowStock, isOutOfStock, type ProductSummary} from '../../types/products'
import {updateProductMinStock} from '../../services/inventoryService'

interface InventoryDetailPanelProps {
    product: ProductSummary | null
    onMinStockUpdated: () => void   // triggers a reload so the alerts panel refreshes
}

function formatDate(value: string | undefined | null): string {
    if (!value) return 'Sin fecha'
    return value
}

export default function InventoryDetailPanel({product, onMinStockUpdated}: InventoryDetailPanelProps) {
    if (!product) {
        return (
            <section className="ui-card p-4">
                <h2 className="ui-title mt-0 text-lg font-semibold">Detalle del producto</h2>
                <p className="ui-muted text-sm">Selecciona un producto para ver su informacion detallada.</p>
            </section>
        )
    }

    const stock = Number(product.stock || 0)
    const suppliers = Array.isArray(product.suppliers) ? product.suppliers : []
    const low = isLowStock(product)
    const out = isOutOfStock(product)

    return (
        <section className="ui-card p-4">
            <h2 className="ui-title mt-0 text-lg font-semibold">Detalle del producto</h2>

            <div className="grid gap-4">
                {/* Image */}
                <div className="overflow-hidden rounded-2xl border border-slate-200 dark:border-slate-700">
                    {product.imageUrl ? (
                        <img src={product.imageUrl} alt={product.name} className="h-64 w-full object-cover" />
                    ) : (
                        <div className="flex h-64 items-center justify-center bg-slate-50 text-sm text-slate-400 dark:bg-slate-800">
                            Sin imagen disponible
                        </div>
                    )}
                </div>

                {/* Name + category */}
                <div>
                    <h3 className="mb-1 mt-0 text-xl font-semibold text-slate-900 dark:text-slate-100">{product.name}</h3>
                    <p className="mt-0 text-sm text-slate-500">{toCategoryLabel(product.category)}</p>
                </div>

                {/* Stock alert badge */}
                {(low || out) && (
                    <div className={`flex items-center gap-2 rounded-xl px-4 py-3 text-sm font-medium ${
                        out
                            ? 'bg-red-50 text-red-700 dark:bg-red-950/40 dark:text-red-400'
                            : 'bg-amber-50 text-amber-800 dark:bg-amber-950/30 dark:text-amber-300'
                    }`}>
                        <span>{out ? '🔴' : '🟡'}</span>
                        <span>
                            {out
                                ? 'Sin stock disponible'
                                : `Stock bajo — quedan ${stock} unidades (umbral: ${product.minStock})`}
                        </span>
                    </div>
                )}

                {/* Stats grid */}
                <div className="grid gap-3 sm:grid-cols-3">
                    <div className="rounded-xl border border-slate-200 p-3 dark:border-slate-700">
                        <div className="text-xs uppercase tracking-wide text-slate-500">Precio</div>
                        <div className="mt-1 text-base font-semibold text-slate-900 dark:text-slate-100">{formatPriceMxn(product.price)}</div>
                    </div>
                    <div className="rounded-xl border border-slate-200 p-3 dark:border-slate-700">
                        <div className="text-xs uppercase tracking-wide text-slate-500">Stock</div>
                        <div className={`mt-1 text-base font-semibold ${out ? 'text-red-600' : low ? 'text-amber-600' : 'text-emerald-600'}`}>
                            {stock} unidades
                        </div>
                    </div>
                    <div className="rounded-xl border border-slate-200 p-3 dark:border-slate-700">
                        <div className="text-xs uppercase tracking-wide text-slate-500">Código</div>
                        <div className="mt-1 text-base font-semibold text-slate-900 dark:text-slate-100">{product.barcode || 'Sin código'}</div>
                    </div>
                </div>

                {/* Min stock editor */}
                <MinStockEditor
                    barcode={product.barcode}
                    currentMinStock={product.minStock}
                    onSaved={onMinStockUpdated}
                />

                {/* Description */}
                <div>
                    <h4 className="ui-title mb-1 mt-0 text-sm font-semibold">Descripción</h4>
                    <p className="ui-muted mt-0 text-sm">
                        {product.description || 'Este producto no tiene una descripción registrada.'}
                    </p>
                </div>

                {/* Suppliers & batches */}
                <div>
                    <h4 className="ui-title mb-2 mt-0 text-sm font-semibold">Proveedores y lotes</h4>
                    {suppliers.length > 0 ? (
                        <div className="grid gap-3">
                            {suppliers.map((supplier) => (
                                <article
                                    key={`${product.barcode}-${supplier.name}`}
                                    className="rounded-xl border border-slate-200 p-3 dark:border-slate-700"
                                >
                                    <div className="flex flex-wrap items-center justify-between gap-2">
                                        <strong className="text-slate-900 dark:text-slate-100">{supplier.name}</strong>
                                        <span className="text-sm text-slate-500">Precio proveedor: {formatPriceMxn(supplier.supplyPrice)}</span>
                                    </div>

                                    <div className="mt-2 grid gap-1.5">
                                        {supplier.batches.length > 0 ? (
                                            supplier.batches.map((batch) => (
                                                <div
                                                    key={`${supplier.name}-${batch.id}`}
                                                    className="flex flex-wrap items-center justify-between gap-2 text-xs text-slate-600 dark:text-slate-300"
                                                >
                                                    <span>
                                                        Lote {batch.code} · {formatDate(batch.receptionDate)} → {formatDate(batch.expirationDate)}
                                                    </span>
                                                    <span>
                                                        Restante: {batch.remainingAmount}/{batch.receivedAmount} · {batch.available ? 'Disponible' : 'No disponible'}
                                                    </span>
                                                </div>
                                            ))
                                        ) : (
                                            <p className="ui-muted mb-0 text-sm">Este proveedor no tiene lotes registrados para el producto.</p>
                                        )}
                                    </div>
                                </article>
                            ))}
                        </div>
                    ) : (
                        <p className="ui-muted mt-0 text-sm">No existen proveedores asociados para este producto.</p>
                    )}
                </div>
            </div>
        </section>
    )
}

// ─── Inline min-stock editor ──────────────────────────────────────────────────

interface MinStockEditorProps {
    barcode: string
    currentMinStock: number | undefined
    onSaved: () => void
}

function MinStockEditor({barcode, currentMinStock, onSaved}: MinStockEditorProps) {
    const [editing, setEditing]   = useState(false)
    const [value, setValue]       = useState(String(currentMinStock ?? ''))
    const [saving, setSaving]     = useState(false)
    const [error, setError]       = useState('')

    const handleSave = async () => {
        const parsed = value.trim() === '' ? null : parseInt(value, 10)
        if (parsed !== null && (isNaN(parsed) || parsed < 0)) {
            setError('Ingresa un número válido mayor o igual a 0')
            return
        }

        setSaving(true)
        setError('')
        try {
            await updateProductMinStock(barcode, parsed)
            setEditing(false)
            onSaved()
        } catch {
            setError('No se pudo guardar. Intenta de nuevo.')
        } finally {
            setSaving(false)
        }
    }

    const handleCancel = () => {
        setValue(String(currentMinStock ?? ''))
        setError('')
        setEditing(false)
    }

    return (
        <div className="rounded-xl border border-slate-200 p-3 dark:border-slate-700">
            <div className="flex items-center justify-between gap-2">
                <div>
                    <div className="text-xs uppercase tracking-wide text-slate-500">Alerta de stock mínimo</div>
                    {!editing && (
                        <div className="mt-1 text-sm text-slate-900 dark:text-slate-100">
                            {currentMinStock != null
                                ? `Alerta cuando stock ≤ ${currentMinStock}`
                                : <span className="ui-muted">Sin umbral configurado</span>}
                        </div>
                    )}
                </div>
                {!editing && (
                    <button
                        type="button"
                        className="ui-btn-ghost text-xs"
                        onClick={() => {
                            setValue(String(currentMinStock ?? ''))
                            setEditing(true)
                        }}
                    >
                        {currentMinStock != null ? 'Editar' : 'Configurar'}
                    </button>
                )}
            </div>

            {editing && (
                <div className="mt-3 grid gap-2">
                    <div className="flex items-center gap-2">
                        <input
                            className="ui-input w-28"
                            type="number"
                            min="0"
                            placeholder="Ej. 5"
                            value={value}
                            disabled={saving}
                            onChange={(e) => setValue(e.target.value)}
                        />
                        <span className="text-xs text-slate-500">unidades</span>
                    </div>
                    <p className="ui-muted mt-0 text-xs">
                        Deja vacío para quitar el umbral y no recibir alertas para este producto.
                    </p>
                    {error && <p className="text-xs text-red-600">{error}</p>}
                    <div className="flex gap-2">
                        <button className="ui-btn-primary text-sm" type="button" disabled={saving} onClick={handleSave}>
                            {saving ? 'Guardando...' : 'Guardar'}
                        </button>
                        <button className="ui-btn-ghost text-sm" type="button" disabled={saving} onClick={handleCancel}>
                            Cancelar
                        </button>
                    </div>
                </div>
            )}
        </div>
    )
}