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
        <div className="card" style={{padding: 16}}>
            <h2 style={{marginTop: 0, fontSize: 18}}>Lista de proveedores</h2>
            {loadingList && <p className="text-muted">Cargando proveedores...</p>}
            {!loadingList && listError && <p className="text-muted">{listError}</p>}
            {!loadingList && !listError && suppliers.length === 0 && (
                <p className="text-muted">No hay proveedores registrados.</p>
            )}

            <div style={{display: 'grid', gap: 8}}>
                {suppliers.map((supplier) => (
                    <button
                        key={supplier.name}
                        type="button"
                        onClick={() => onSupplierClick(supplier.name)}
                        style={{
                            textAlign: 'left',
                            border: selectedName === supplier.name ? '1px solid var(--accent)' : '1px solid var(--border)',
                            borderRadius: 8,
                            background: selectedName === supplier.name ? 'var(--active-bg)' : 'white',
                            padding: 12,
                            cursor: 'pointer',
                            color: 'inherit',
                        }}
                    >
                        <div style={{fontWeight: 600}}>{supplier.name}</div>
                        <div className="text-muted" style={{fontSize: 13}}>{supplier.contactEmail}</div>
                    </button>
                ))}
            </div>
        </div>
    )
}

