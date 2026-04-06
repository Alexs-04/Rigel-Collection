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
            <h2 className="mt-0 text-lg font-semibold text-slate-900">Lista de proveedores</h2>
            {loadingList && <p className="text-sm text-slate-500">Cargando proveedores...</p>}
            {!loadingList && listError && <p className="text-sm text-slate-500">{listError}</p>}
            {!loadingList && !listError && suppliers.length === 0 && (
                <p className="text-sm text-slate-500">No hay proveedores registrados.</p>
            )}

            <div className="grid gap-2">
                {suppliers.map((supplier) => (
                    <button
                        key={supplier.name}
                        type="button"
                        onClick={() => onSupplierClick(supplier.name)}
                        className={`rounded-lg border p-3 text-left text-inherit transition ${
                            selectedName === supplier.name
                                ? 'border-brand-500 bg-brand-50'
                                : 'border-slate-200 bg-white hover:border-brand-200'
                        }`}
                    >
                        <div className="font-semibold text-slate-900">{supplier.name}</div>
                        <div className="text-xs text-slate-500">{supplier.contactEmail}</div>
                    </button>
                ))}
            </div>
        </div>
    )
}

