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
        <section className="card" style={{padding: 16, display: 'grid', gap: 12}}>
            <h2 style={{margin: 0, fontSize: 18}}>Catalogo</h2>

            <input
                className="input"
                placeholder="Buscar por nombre, codigo de barras o categoria"
                value={search}
                onChange={(event) => onSearchChange(event.target.value)}
            />

            {loading && <p className="text-muted" style={{margin: 0}}>Cargando productos...</p>}
            {!loading && error && <p style={{margin: 0, color: '#dc2626'}}>{error}</p>}

            {!loading && !error && products.length === 0 && (
                <p className="text-muted" style={{margin: 0}}>No hay productos disponibles para vender.</p>
            )}

            {!loading && !error && products.length > 0 && (
                <div style={{display: 'grid', gap: 10, maxHeight: 560, overflowY: 'auto', paddingRight: 4}}>
                    {products.map((item) => {
                        const soldOut = item.availableUnits <= 0

                        return (
                            <article
                                key={item.barcode || item.name}
                                style={{
                                    border: '1px solid rgba(2, 6, 23, 0.08)',
                                    borderRadius: 10,
                                    padding: 12,
                                    display: 'grid',
                                    gap: 8,
                                }}
                            >
                                <div style={{display: 'flex', justifyContent: 'space-between', gap: 12}}>
                                    <div>
                                        <strong>{item.name}</strong>
                                        <p className="text-muted" style={{margin: '2px 0 0'}}>
                                            {toCategoryLabel(item.category)}
                                        </p>
                                    </div>
                                    <strong>{formatPriceMxn(item.price)}</strong>
                                </div>

                                <p className="text-muted" style={{margin: 0, fontSize: 13}}>
                                    {item.description || 'Sin descripcion'}
                                </p>

                                <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 12}}>
                                    <small className="text-muted">Disponibles: {item.availableUnits}</small>
                                    <button
                                        className="btn-primary"
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

