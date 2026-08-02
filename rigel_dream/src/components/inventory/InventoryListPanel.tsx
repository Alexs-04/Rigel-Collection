import {formatPriceMxn, toCategoryLabel} from '../../utils/productPresentation'
import {isLowStock, isOutOfStock, type ProductSummary} from '../../types/products'

interface InventoryListPanelProps {
    loading: boolean
    error: string
    products: ProductSummary[]
    selectedBarcode: string
    onSelectProduct: (product: ProductSummary) => void
}

function getStockLabel(product: ProductSummary): string {
    const stock = product.stock
    if (stock <= 0) return 'Sin stock'
    if (isLowStock(product)) return `Bajo stock: ${stock}`
    return `Stock: ${stock}`
}

function getStockClass(product: ProductSummary): string {
    if (isOutOfStock(product)) return 'text-red-600'
    if (isLowStock(product)) return 'text-amber-600'
    return 'text-emerald-600'
}

function getThumbnailClass(product: ProductSummary): string {
    return isOutOfStock(product) ? 'bg-slate-50' : 'bg-slate-100'
}

export default function InventoryListPanel({
                                               loading,
                                               error,
                                               products,
                                               selectedBarcode,
                                               onSelectProduct,
                                           }: InventoryListPanelProps) {
    return (
        <section className="ui-card p-4">
            <h2 className="ui-title mt-0 text-lg font-semibold">Productos registrados</h2>

            {loading && <p className="ui-muted text-sm">Cargando inventario...</p>}
            {!loading && error && <p className="ui-muted text-sm text-red-600">{error}</p>}
            {!loading && !error && products.length === 0 && (
                <p className="ui-muted text-sm">No se encontraron productos con ese criterio.</p>
            )}

            <div className="grid gap-2">
                {products.map((product) => {
                    const isSelected = selectedBarcode === product.barcode
                    const low = isLowStock(product)
                    const out = isOutOfStock(product)

                    // Add a subtle left accent for low/out of stock items
                    const accentClass = out
                        ? 'border-l-2 border-l-red-400'
                        : low
                            ? 'border-l-2 border-l-amber-400'
                            : ''

                    return (
                        <button
                            key={product.barcode || product.name}
                            type="button"
                            onClick={() => onSelectProduct(product)}
                            className={`grid gap-3 rounded-xl border p-3 text-left transition hover:border-brand-300 hover:bg-brand-50/30 ${accentClass} ${
                                isSelected
                                    ? 'border-brand-400 bg-brand-50/50 dark:border-brand-500 dark:bg-brand-500/10'
                                    : 'border-slate-200 dark:border-slate-700'
                            }`}
                        >
                            <div className="flex gap-3">
                                <div
                                    className={`flex h-16 w-16 shrink-0 items-center justify-center overflow-hidden rounded-lg border border-slate-200 ${getThumbnailClass(product)}`}>
                                    {product.imageUrl ? (
                                        <img
                                            src={product.imageUrl}
                                            alt={product.name}
                                            className="h-full w-full object-cover"
                                        />
                                    ) : (
                                        <span
                                            className="text-[10px] uppercase tracking-wide text-slate-400">Sin imagen</span>
                                    )}
                                </div>

                                <div className="min-w-0 flex-1">
                                    <div className="flex items-start justify-between gap-2">
                                        <div className="min-w-0">
                                            <div
                                                className="truncate font-semibold text-slate-900 dark:text-slate-100">{product.name}</div>
                                            <div
                                                className="text-xs text-slate-500">{toCategoryLabel(product.category)}</div>
                                        </div>
                                        <div
                                            className="shrink-0 text-right text-sm font-semibold text-slate-900 dark:text-slate-100">
                                            {formatPriceMxn(product.price)}
                                        </div>
                                    </div>

                                    <p className="mt-1 line-clamp-2 text-xs text-slate-500">
                                        {product.description || 'Sin descripción disponible.'}
                                    </p>
                                </div>
                            </div>

                            <div className="flex flex-wrap items-center justify-between gap-2 text-xs text-slate-500">
                                <span>{product.barcode || 'Sin código de barras'}</span>
                                <strong className={getStockClass(product)}>{getStockLabel(product)}</strong>
                            </div>
                        </button>
                    )
                })}
            </div>
        </section>
    )
}