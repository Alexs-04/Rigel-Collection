import type {SupplierSummary} from '../../types/suppliers'

interface SuppliersListPanelProps {
    loadingList: boolean
    listError: string
    suppliers: SupplierSummary[]
    selectedName: string | null
    onSupplierClick: (name: string) => void
}

export default function SuppliersListPanel({
    loadingList,
    listError,
    suppliers,
    selectedName,
    onSupplierClick,
}: SuppliersListPanelProps) {
    return (
        <div className="ui-card p-4">
            <h2 className="ui-title mt-0 text-lg font-semibold">Lista de proveedores</h2>
            {loadingList && <p className="ui-muted text-sm">Cargando proveedores...</p>}
            {!loadingList && listError && <p className="ui-muted text-sm">{listError}</p>}
            {!loadingList && !listError && suppliers.length === 0 && (
                <p className="ui-muted text-sm">No hay proveedores registrados.</p>
            )}

            <div className="grid gap-2">
                {suppliers.map((supplier) => (
                    <button
                        key={supplier.name}
                        type="button"
                        onClick={() => onSupplierClick(supplier.name)}
                        className={`ui-list-item ${selectedName === supplier.name ? 'ui-list-item-active' : ''}`}
                    >
                        <div className="ui-title font-semibold">{supplier.name}</div>
                        <div className="ui-muted text-xs">{supplier.contactEmail}</div>
                    </button>
                ))}
            </div>
        </div>
    )
}

