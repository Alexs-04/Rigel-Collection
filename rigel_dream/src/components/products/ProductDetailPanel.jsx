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
        <div className="card product-detail-card" style={{padding: 16}}>
            <h2 style={{marginTop: 0, fontSize: 18}}>Detalle del producto</h2>
            {!selectedName && !isCollapsingDetail && (
                <p className="text-muted">Selecciona un producto para ver su informacion.</p>
            )}

            <div className={`product-detail-body ${detailExpanded ? 'is-open' : ''}`}>
                {showDetailContent && (
                    <>
                        {loadingDetail && <p className="text-muted">Cargando detalle...</p>}
                        {detailError && <p className="text-muted">{detailError}</p>}

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
                                    <div style={{display: 'flex', gap: 8, marginBottom: 12}}>
                                        {!editing && (
                                            <button className="btn-primary" type="button" onClick={onStartEdit}>
                                                Editar
                                            </button>
                                        )}
                                        {editing && (
                                            <button className="btn-primary" type="button" disabled={saving} onClick={onSaveChanges}>
                                                Guardar cambios
                                            </button>
                                        )}
                                        {editing && (
                                            <button className="btn-ghost" type="button" onClick={onCancelEdit}>
                                                Cancelar
                                            </button>
                                        )}
                                        <button className="btn-ghost" type="button" onClick={onDeleteProduct}>
                                            Eliminar
                                        </button>
                                    </div>
                                )}

                                <h3 style={{marginBottom: 8}}>Proveedor(es) asociado(s)</h3>
                                {Array.isArray(detail.suppliers) && detail.suppliers.length > 0 ? (
                                    <div style={{display: 'grid', gap: 8}}>
                                        {detail.suppliers.map((supplier) => (
                                            <div
                                                key={`${detail.name}-${supplier.name}`}
                                                style={{border: '1px solid var(--border)', borderRadius: 8, padding: 10}}
                                            >
                                                <div style={{fontWeight: 600}}>{supplier.name}</div>
                                                <div className="text-muted" style={{fontSize: 13}}>
                                                    Precio proveedor: {formatPriceMxn(supplier.supplyPrice)}
                                                </div>

                                                {Array.isArray(supplier.batches) && supplier.batches.length > 0 && (
                                                    <div style={{display: 'grid', gap: 6, marginTop: 8}}>
                                                        {supplier.batches.map((batch) => (
                                                            <div key={`${supplier.name}-${batch.id}`} style={{fontSize: 13}}>
                                                                Lote {batch.code} - restante {batch.remainingAmount}/{batch.receivedAmount} - {batch.available ? 'Disponible' : 'No disponible'}
                                                            </div>
                                                        ))}
                                                    </div>
                                                )}
                                            </div>
                                        ))}
                                    </div>
                                ) : (
                                    <p className="text-muted" style={{marginTop: 0}}>
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

