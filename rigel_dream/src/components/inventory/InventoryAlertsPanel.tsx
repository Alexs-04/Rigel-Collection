import {useState} from 'react'
import type {ProductSummary} from '../../types/products.ts'

interface InventoryAlertsPanelProps {
    products: ProductSummary[]
    onSelectProduct: (barcode: string) => void
}

export default function InventoryAlertsPanel({products, onSelectProduct}: InventoryAlertsPanelProps) {
    const [expanded, setExpanded] = useState(true)

    if (products.length === 0) return null

    const outOfStock = products.filter((p) => p.stock <= 0)
    const lowStock   = products.filter((p) => p.stock > 0)

    return (
        <section className="rounded-xl border border-amber-200 bg-amber-50 dark:border-amber-800 dark:bg-amber-950/30">
            {/* Header */}
            <button
                type="button"
                onClick={() => setExpanded((v) => !v)}
                className="flex w-full items-center justify-between gap-3 px-4 py-3 text-left"
            >
                <div className="flex items-center gap-2.5">
                    <span className="text-lg">⚠️</span>
                    <div>
                        <span className="font-semibold text-amber-900 dark:text-amber-200">
                            {products.length === 1
                                ? '1 producto requiere atención'
                                : `${products.length} productos requieren atención`}
                        </span>
                        <div className="flex gap-3 mt-0.5">
                            {outOfStock.length > 0 && (
                                <span className="text-xs font-medium text-red-600 dark:text-red-400">
                                    {outOfStock.length} sin stock
                                </span>
                            )}
                            {lowStock.length > 0 && (
                                <span className="text-xs font-medium text-amber-700 dark:text-amber-400">
                                    {lowStock.length} bajo umbral
                                </span>
                            )}
                        </div>
                    </div>
                </div>
                <span className="text-amber-600 dark:text-amber-400 text-sm select-none">
                    {expanded ? '▲ Ocultar' : '▼ Ver'}
                </span>
            </button>

            {/* Product list */}
            {expanded && (
                <div className="border-t border-amber-200 dark:border-amber-800 px-4 pb-4 pt-3">
                    <div className="grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
                        {/* Out of stock first */}
                        {outOfStock.map((product) => (
                            <AlertCard
                                key={product.barcode}
                                product={product}
                                variant="danger"
                                onSelect={onSelectProduct}
                            />
                        ))}
                        {/* Then low stock */}
                        {lowStock.map((product) => (
                            <AlertCard
                                key={product.barcode}
                                product={product}
                                variant="warning"
                                onSelect={onSelectProduct}
                            />
                        ))}
                    </div>
                </div>
            )}
        </section>
    )
}

// ─── Alert card ───────────────────────────────────────────────────────────────

interface AlertCardProps {
    product: ProductSummary
    variant: 'danger' | 'warning'
    onSelect: (barcode: string) => void
}

function AlertCard({product, variant, onSelect}: AlertCardProps) {
    const isDanger = variant === 'danger'

    const borderClass = isDanger
        ? 'border-red-200 bg-red-50 dark:border-red-800 dark:bg-red-950/40'
        : 'border-amber-200 bg-white dark:border-amber-700 dark:bg-slate-900'

    const stockClass = isDanger ? 'text-red-600 dark:text-red-400' : 'text-amber-700 dark:text-amber-400'

    const stockLabel = isDanger
        ? 'Sin stock'
        : `Stock: ${product.stock} (umbral: ${product.minStock})`

    return (
        <button
            type="button"
            onClick={() => onSelect(product.barcode)}
            className={`flex items-center gap-3 rounded-xl border p-3 text-left transition hover:opacity-80 ${borderClass}`}
        >
            {/* Thumbnail */}
            <div className="h-10 w-10 shrink-0 overflow-hidden rounded-lg border border-slate-200 bg-slate-100 dark:border-slate-700">
                {product.imageUrl ? (
                    <img src={product.imageUrl} alt={product.name} className="h-full w-full object-cover" />
                ) : (
                    <div className="flex h-full w-full items-center justify-center text-[8px] uppercase tracking-wide text-slate-400">
                        N/A
                    </div>
                )}
            </div>

            {/* Info */}
            <div className="min-w-0 flex-1">
                <div className="truncate text-sm font-semibold text-slate-900 dark:text-slate-100">
                    {product.name}
                </div>
                <div className={`text-xs font-medium ${stockClass}`}>{stockLabel}</div>
            </div>
        </button>
    )
}