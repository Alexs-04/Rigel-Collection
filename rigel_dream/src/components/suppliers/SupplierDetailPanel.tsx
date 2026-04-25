import type {SupplierDetail, SupplierFormValues, SupplierProduct} from '../../types/suppliers'
import SupplierFieldsGrid from './SupplierFieldsGrid'
import SupplierProductsList from './SupplierProductsList'

type SupplierDetailField = keyof Pick<SupplierFormValues, 'name' | 'contactEmail' | 'phoneNumber' | 'address'>

interface SupplierDetailPanelProps {
    selectedName: string | null
    isCollapsingDetail: boolean
    detailExpanded: boolean
    showDetailContent: boolean
    loadingDetail: boolean
    detailError: string
    detail: SupplierDetail | null
    products: SupplierProduct[]
    editing: boolean
    saving: boolean
    isAdmin: boolean
    onChangeDetail: (key: SupplierDetailField, value: string) => void
    onStartEdit: () => void
    onCancelEdit: () => void
    onSaveChanges: () => void
    onDeleteSupplier: () => void
}

export default function SupplierDetailPanel({
    selectedName,
    isCollapsingDetail,
    detailExpanded,
    showDetailContent,
    loadingDetail,
    detailError,
    detail,
    products,
    editing,
    saving,
    isAdmin,
    onChangeDetail,
    onStartEdit,
    onCancelEdit,
    onSaveChanges,
    onDeleteSupplier,
}: SupplierDetailPanelProps) {
    return (
        <div className="ui-card supplier-detail-card p-4">
            <h2 className="ui-title mt-0 text-lg font-semibold">Detalle del proveedor</h2>
            {!selectedName && !isCollapsingDetail && (
                <p className="ui-muted text-sm">Selecciona un proveedor para ver su informacion.</p>
            )}

            <div className={`supplier-detail-body ${detailExpanded ? 'is-open' : ''}`}>
                {showDetailContent && (
                    <>
                        {loadingDetail && <p className="ui-muted text-sm">Cargando detalle...</p>}
                        {detailError && <p className="ui-muted text-sm">{detailError}</p>}

                        {detail && !loadingDetail && (
                            <>
                                <SupplierFieldsGrid
                                    value={detail}
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
                                        <button className="ui-btn-ghost" type="button" onClick={onDeleteSupplier}>
                                            Eliminar
                                        </button>
                                    </div>
                                )}

                                <h3 className="ui-title mb-2 text-base font-semibold">Productos asociados</h3>
                                <SupplierProductsList products={products} />
                            </>
                        )}
                    </>
                )}
            </div>
        </div>
    )
}

