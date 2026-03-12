import React from 'react'
import {formatPriceMxn, toCategoryLabel} from '../../../utils/productPresentation'

export default function ProductsListPanel({
    search,
    onSearchChange,
    loadingList,
    listError,
    products,
    selectedName,
    onProductClick,
}) {
    const isFiltered = Boolean(search.trim())

    return (
        <div className="card" style={{padding: 16}}>
            <h2 style={{marginTop: 0, fontSize: 18}}>Lista de productos</h2>
            <input
                className="input"
                placeholder="Buscar por nombre, codigo o categoria"
                value={search}
                onChange={(e) => onSearchChange(e.target.value)}
                style={{marginBottom: 12}}
            />

            {loadingList && <p className="text-muted">Cargando productos...</p>}
            {!loadingList && listError && <p className="text-muted">{listError}</p>}
            {!loadingList && !listError && products.length === 0 && (
                <p className="text-muted">
                    {isFiltered ? 'No se encontraron productos con ese criterio.' : 'No hay productos registrados.'}
                </p>
            )}

            <div style={{display: 'grid', gap: 8}}>
                {products.map((product) => (
                    <button
                        key={product.barcode || product.name}
                        type="button"
                        onClick={() => onProductClick(product.name)}
                        style={{
                            textAlign: 'left',
                            border: selectedName === product.name ? '1px solid var(--accent)' : '1px solid var(--border)',
                            borderRadius: 8,
                            background: selectedName === product.name ? 'var(--active-bg)' : 'white',
                            padding: 12,
                            cursor: 'pointer',
                            color: 'inherit',
                        }}
                    >
                        <div style={{fontWeight: 600}}>{product.name}</div>
                        <div className="text-muted" style={{fontSize: 13}}>
                            {toCategoryLabel(product.category || 'OTHERS')} - {formatPriceMxn(product.price)}
                        </div>
                    </button>
                ))}
            </div>
        </div>
    )
}

