import React from 'react'
import {formatPriceMxn, toCategoryLabel} from '../../utils/productPresentation.js'

export default function ProductsListPanel({
    loadingList,
    listError,
    products,
    selectedName,
    onProductClick,
}) {
    return (
        <div className="ui-card p-4">
            <h2 className="ui-title mt-0 text-lg font-semibold">Lista de productos</h2>

            {loadingList && <p className="ui-muted text-sm">Cargando productos...</p>}
            {!loadingList && listError && <p className="ui-muted text-sm">{listError}</p>}
            {!loadingList && !listError && products.length === 0 && (
                <p className="ui-muted text-sm">
                    No se encontraron productos con ese criterio.
                </p>
            )}

            <div className="grid gap-2">
                {products.map((product) => (
                    <button
                        key={product.barcode || product.name}
                        type="button"
                        onClick={() => onProductClick(product.name)}
                        className={`ui-list-item ${selectedName === product.name ? 'ui-list-item-active' : ''}`}
                    >
                        <div className="ui-title font-semibold">{product.name}</div>
                        <div className="ui-muted text-xs">
                            {toCategoryLabel(product.category || 'OTHERS')} - {formatPriceMxn(product.price)}
                        </div>
                    </button>
                ))}
            </div>
        </div>
    )
}

