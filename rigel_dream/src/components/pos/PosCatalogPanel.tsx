import {toCategoryLabel, formatPriceMxn} from '../../utils/productPresentation'
import type {PosCatalogItem} from '../../types/pos'

interface PosCatalogPanelProps {
    search: string
    onSearchChange: (value: string) => void
    loading: boolean
    error: string
    products: PosCatalogItem[]
    onAddProduct: (item: PosCatalogItem) => void
}

export default function PosCatalogPanel({
    search,
    onSearchChange,
    loading,
    error,
    products,
    onAddProduct,
}: PosCatalogPanelProps) {
    return (
        <section className="ui-card grid gap-3 p-4">
            <h2 className="m-0 text-lg font-semibold text-slate-900">Catalogo</h2>

            <input
                className="ui-input"
                placeholder="Buscar por nombre, codigo de barras o categoria"
                value={search}
                onChange={(event) => onSearchChange(event.target.value)}
            />

            {loading && <p className="m-0 text-sm text-slate-500">Cargando productos...</p>}
            {!loading && error && <p className="m-0 text-sm text-red-600">{error}</p>}

            {!loading && !error && products.length === 0 && (
                <p className="m-0 text-sm text-slate-500">No hay productos disponibles para vender.</p>
            )}

            {!loading && !error && products.length > 0 && (
                <div className="grid max-h-[35rem] gap-2.5 overflow-y-auto pr-1">
                    {products.map((item) => {
                        const soldOut = item.availableUnits <= 0

                        return (
                            <article
                                key={item.barcode || item.name}
                                className="grid gap-2 rounded-lg border border-slate-200 p-3"
                            >
                                <div className="flex items-start justify-between gap-3">
                                    <div>
                                        <strong className="text-slate-900">{item.name}</strong>
                                        <p className="mb-0 mt-0.5 text-xs text-slate-500">
                                            {toCategoryLabel(item.category)}
                                        </p>
                                    </div>
                                    <strong className="text-slate-900">{formatPriceMxn(item.price)}</strong>
                                </div>

                                <p className="m-0 text-xs text-slate-500">
                                    {item.description || 'Sin descripcion'}
                                </p>

                                <div className="flex items-center justify-between gap-3">
                                    <small className="text-xs text-slate-500">Disponibles: {item.availableUnits}</small>
                                    <button
                                        className="ui-btn-primary"
                                        type="button"
                                        onClick={() => onAddProduct(item)}
                                        disabled={soldOut}
                                    >
                                        {soldOut ? 'Sin stock' : 'Agregar'}
                                    </button>
                                </div>
                            </article>
                        )
                    })}
                </div>
            )}
        </section>
    )
}

