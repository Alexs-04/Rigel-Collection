import {formatPriceMxn, toCategoryLabel} from '../../utils/productPresentation'
import type {ProductSummary} from '../../types/products'

interface InventoryDetailPanelProps {
    product: ProductSummary | null
}

function formatDate(value: string | undefined | null): string {
    if (!value) return 'Sin fecha'
    return value
}

export default function InventoryDetailPanel({product}: InventoryDetailPanelProps) {
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

    return (
        <section className="ui-card p-4">
            <h2 className="ui-title mt-0 text-lg font-semibold">Detalle del producto</h2>

            <div className="grid gap-4">
                <div className="overflow-hidden rounded-2xl border border-slate-200 dark:border-slate-700">
                    {product.imageUrl ? (
                        <img
                            src={product.imageUrl}
                            alt={product.name}
                            className="h-64 w-full object-cover"
                        />
                    ) : (
                        <div className="flex h-64 items-center justify-center bg-slate-50 text-sm text-slate-400 dark:bg-slate-800">
                            Sin imagen disponible
                        </div>
                    )}
                </div>

                <div>
                    <h3 className="mb-1 mt-0 text-xl font-semibold text-slate-900 dark:text-slate-100">{product.name}</h3>
                    <p className="mt-0 text-sm text-slate-500">{toCategoryLabel(product.category)}</p>
                </div>

                <div className="grid gap-3 sm:grid-cols-3">
                    <div className="rounded-xl border border-slate-200 p-3 dark:border-slate-700">
                        <div className="text-xs uppercase tracking-wide text-slate-500">Precio</div>
                        <div className="mt-1 text-base font-semibold text-slate-900 dark:text-slate-100">{formatPriceMxn(product.price)}</div>
                    </div>
                    <div className="rounded-xl border border-slate-200 p-3 dark:border-slate-700">
                        <div className="text-xs uppercase tracking-wide text-slate-500">Stock</div>
                        <div className={`mt-1 text-base font-semibold ${stock <= 0 ? 'text-red-600' : 'text-emerald-600'}`}>
                            {stock} unidades
                        </div>
                    </div>
                    <div className="rounded-xl border border-slate-200 p-3 dark:border-slate-700">
                        <div className="text-xs uppercase tracking-wide text-slate-500">Código</div>
                        <div className="mt-1 text-base font-semibold text-slate-900 dark:text-slate-100">{product.barcode || 'Sin código'}</div>
                    </div>
                </div>

                <div>
                    <h4 className="ui-title mb-1 mt-0 text-sm font-semibold">Descripción</h4>
                    <p className="ui-muted mt-0 text-sm">
                        {product.description || 'Este producto no tiene una descripción registrada.'}
                    </p>
                </div>

                <div>
                    <h4 className="ui-title mb-2 mt-0 text-sm font-semibold">Proveedores y lotes</h4>
                    {suppliers.length > 0 ? (
                        <div className="grid gap-3">
                            {suppliers.map((supplier) => (
                                <article key={`${product.barcode}-${supplier.name}`} className="rounded-xl border border-slate-200 p-3 dark:border-slate-700">
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

