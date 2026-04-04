import React from 'react'
import {formatPriceMxn} from '../../utils/productPresentation.js'
import ProductFieldsGrid from './ProductFieldsGrid.jsx'

export default function ProductDetailPanel({
    selectedName,
    isCollapsingDetail,
    detailExpanded,
    showDetailContent,
    loadingDetail,
    detailError,
    detail,
    editing,
    saving,
    isAdmin,
    suppliers,
    onChangeDetail,
    onStartEdit,
    onCancelEdit,
    onSaveChanges,
    onDeleteProduct,
}) {
    return (
        <div className="ui-card product-detail-card p-4">
            <h2 className="mt-0 text-lg font-semibold text-slate-900">Detalle del producto</h2>
            {!selectedName && !isCollapsingDetail && (
                <p className="text-sm text-slate-500">Selecciona un producto para ver su informacion.</p>
            )}

            <div className={`product-detail-body ${detailExpanded ? 'is-open' : ''}`}>
                {showDetailContent && (
                    <>
                        {loadingDetail && <p className="text-sm text-slate-500">Cargando detalle...</p>}
                        {detailError && <p className="text-sm text-slate-500">{detailError}</p>}

                        {detail && !loadingDetail && (
                            <>
                                <ProductFieldsGrid
                                    value={detail}
                                    suppliers={suppliers}
                                    disabled={!editing}
                                    isRequired={editing}
                                    onChange={onChangeDetail}
                                />

                                {isAdmin && (
                                    <div className="mb-3 flex flex-wrap gap-2">
                                        {!editing && (
                                            <button className="ui-btn-primary" type="button" onClick={onStartEdit}>
                                                Editar
                                            </button>
                                        )}
                                        {editing && (
                                            <button className="ui-btn-primary" type="button" disabled={saving} onClick={onSaveChanges}>
                                                Guardar cambios
                                            </button>
                                        )}
                                        {editing && (
                                            <button className="ui-btn-ghost" type="button" onClick={onCancelEdit}>
                                                Cancelar
                                            </button>
                                        )}
                                        <button className="ui-btn-ghost" type="button" onClick={onDeleteProduct}>
                                            Eliminar
                                        </button>
                                    </div>
                                )}

                                <h3 className="mb-2 text-base font-semibold text-slate-900">Proveedor(es) asociado(s)</h3>
                                {Array.isArray(detail.suppliers) && detail.suppliers.length > 0 ? (
                                    <div className="grid gap-2">
                                        {detail.suppliers.map((supplier) => (
                                            <div
                                                key={`${detail.name}-${supplier.name}`}
                                                className="rounded-lg border border-slate-200 p-2.5"
                                            >
                                                <div className="font-semibold text-slate-900">{supplier.name}</div>
                                                <div className="text-xs text-slate-500">
                                                    Precio proveedor: {formatPriceMxn(supplier.supplyPrice)}
                                                </div>

                                                {Array.isArray(supplier.batches) && supplier.batches.length > 0 && (
                                                    <div className="mt-2 grid gap-1.5">
                                                        {supplier.batches.map((batch) => (
                                                            <div key={`${supplier.name}-${batch.id}`} className="text-xs text-slate-600">
                                                                Lote {batch.code} - restante {batch.remainingAmount}/{batch.receivedAmount} - {batch.available ? 'Disponible' : 'No disponible'}
                                                            </div>
                                                        ))}
                                                    </div>
                                                )}
                                            </div>
                                        ))}
                                    </div>
                                ) : (
                                    <p className="mt-0 text-sm text-slate-500">
                                        Este producto no tiene proveedores asociados.
                                    </p>
                                )}
                            </>
                        )}
                    </>
                )}
            </div>
        </div>
    )
}

