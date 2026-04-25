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
    managingSuppliers,
    relationForm,
    relationMessage,
    relationSaving,
    availableSuppliers,
    onToggleManageSuppliers,
    onChangeRelationForm,
    onAddSupplier,
    onRemoveSupplier,
}) {
    return (
        <div className="ui-card product-detail-card p-4">
            <h2 className="ui-title mt-0 text-lg font-semibold">Detalle del producto</h2>
            {!selectedName && !isCollapsingDetail && (
                <p className="ui-muted text-sm">Selecciona un producto para ver su informacion.</p>
            )}

            <div className={`product-detail-body ${detailExpanded ? 'is-open' : ''}`}>
                {showDetailContent && (
                    <>
                        {loadingDetail && <p className="ui-muted text-sm">Cargando detalle...</p>}
                        {detailError && <p className="ui-muted text-sm">{detailError}</p>}

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
                                        {!editing && (
                                            <button className="ui-btn-ghost" type="button" onClick={onToggleManageSuppliers}>
                                                {managingSuppliers ? 'Cerrar gestion de proveedores' : 'Gestionar proveedores'}
                                            </button>
                                        )}
                                        <button className="ui-btn-ghost" type="button" onClick={onDeleteProduct}>
                                            Eliminar
                                        </button>
                                    </div>
                                )}

                                <h3 className="ui-title mb-2 text-base font-semibold">Proveedor(es) asociado(s)</h3>
                                {Array.isArray(detail.suppliers) && detail.suppliers.length > 0 ? (
                                    <div className="grid gap-2">
                                        {detail.suppliers.map((supplier) => (
                                            <div
                                                key={`${detail.name}-${supplier.name}`}
                                                className="ui-subcard"
                                            >
                                                <div className="ui-title font-semibold">{supplier.name}</div>
                                                <div className="ui-muted text-xs">
                                                    Precio proveedor: {formatPriceMxn(supplier.supplyPrice)}
                                                </div>
                                                {isAdmin && managingSuppliers && (
                                                    <button
                                                        type="button"
                                                        className="ui-btn-ghost mt-2"
                                                        disabled={relationSaving}
                                                        onClick={() => onRemoveSupplier(supplier.name)}
                                                    >
                                                        Quitar proveedor
                                                    </button>
                                                )}

                                                {Array.isArray(supplier.batches) && supplier.batches.length > 0 && (
                                                    <div className="mt-2 grid gap-1.5">
                                                        {supplier.batches.map((batch) => (
                                                            <div key={`${supplier.name}-${batch.id}`} className="text-xs text-slate-600 dark:text-slate-300">
                                                                Lote {batch.code} - restante {batch.remainingAmount}/{batch.receivedAmount} - {batch.available ? 'Disponible' : 'No disponible'}
                                                            </div>
                                                        ))}
                                                    </div>
                                                )}
                                            </div>
                                        ))}
                                    </div>
                                ) : (
                                    <p className="ui-muted mt-0 text-sm">
                                        Este producto no tiene proveedores asociados.
                                    </p>
                                )}

                                {isAdmin && managingSuppliers && (
                                    <div className="mt-4 rounded-xl border border-slate-200 p-3 dark:border-slate-700 dark:bg-slate-800/60">
                                        <h4 className="ui-title mb-2 mt-0 text-sm font-semibold">
                                            Asociar nuevo proveedor
                                        </h4>
                                        <div className="grid gap-2 md:grid-cols-2">
                                            <select
                                                className="ui-select"
                                                value={relationForm.supplierName}
                                                onChange={(e) => onChangeRelationForm('supplierName', e.target.value)}
                                            >
                                                <option value="">Selecciona proveedor</option>
                                                {availableSuppliers.map((supplier) => (
                                                    <option key={supplier.name} value={supplier.name}>
                                                        {supplier.name}
                                                    </option>
                                                ))}
                                            </select>
                                            <input
                                                className="ui-input"
                                                type="number"
                                                min="0"
                                                step="0.01"
                                                placeholder="Precio proveedor"
                                                value={relationForm.supplierPrice}
                                                onChange={(e) => onChangeRelationForm('supplierPrice', e.target.value)}
                                            />
                                        </div>
                                        <div className="mt-2 flex flex-wrap gap-2">
                                            <button
                                                type="button"
                                                className="ui-btn-primary"
                                                disabled={relationSaving || !availableSuppliers.length}
                                                onClick={onAddSupplier}
                                            >
                                                Agregar proveedor
                                            </button>
                                        </div>
                                        {relationMessage && <p className="ui-muted mb-0 mt-2 text-sm">{relationMessage}</p>}
                                    </div>
                                )}
                            </>
                        )}
                    </>
                )}
            </div>
        </div>
    )
}

