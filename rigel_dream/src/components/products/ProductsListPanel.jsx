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
            <h2 className="mt-0 text-lg font-semibold text-slate-900">Lista de productos</h2>

            {loadingList && <p className="text-sm text-slate-500">Cargando productos...</p>}
            {!loadingList && listError && <p className="text-sm text-slate-500">{listError}</p>}
            {!loadingList && !listError && products.length === 0 && (
                <p className="text-sm text-slate-500">
                    No se encontraron productos con ese criterio.
                </p>
            )}

            <div className="grid gap-2">
                {products.map((product) => (
                    <button
                        key={product.barcode || product.name}
                        type="button"
                        onClick={() => onProductClick(product.name)}
                        className={`rounded-lg border p-3 text-left text-inherit transition ${
                            selectedName === product.name
                                ? 'border-brand-500 bg-brand-50'
                                : 'border-slate-200 bg-white hover:border-brand-200'
                        }`}
                    >
                        <div className="font-semibold text-slate-900">{product.name}</div>
                        <div className="text-xs text-slate-500">
                            {toCategoryLabel(product.category || 'OTHERS')} - {formatPriceMxn(product.price)}
                        </div>
                    </button>
                ))}
            </div>
        </div>
    )
}

