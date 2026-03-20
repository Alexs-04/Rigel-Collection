import {formatPriceMxn} from '../../utils/productPresentation.js'
import type {PurchaseDto} from '../../types/purchases'

interface PurchasesListPanelProps {
    search: string
    onSearchChange: (value: string) => void
    loadingList: boolean
    listError: string
    purchases: PurchaseDto[]
    selectedId: number | null
    onPurchaseClick: (id: number) => void
}

export default function PurchasesListPanel({
    search,
    onSearchChange,
    loadingList,
    listError,
    purchases,
    selectedId,
    onPurchaseClick,
}: PurchasesListPanelProps) {
    return (
        <section className="card" style={{padding: 16}}>
            <h2 style={{marginTop: 0, fontSize: 18}}>Compras registradas</h2>
            <input
                className="input"
                placeholder="Buscar por codigo, producto, proveedor o lote"
                value={search}
                onChange={(e) => onSearchChange(e.target.value)}
                style={{marginBottom: 12}}
            />

            {loadingList && <p className="text-muted">Cargando compras...</p>}
            {!loadingList && listError && <p className="text-muted">{listError}</p>}
            {!loadingList && !listError && purchases.length === 0 && <p className="text-muted">No hay compras registradas.</p>}

            <div style={{display: 'grid', gap: 8}}>
                {purchases.map((purchase) => (
                    <button
                        key={purchase.id}
                        type="button"
                        onClick={() => onPurchaseClick(purchase.id)}
                        style={{
                            textAlign: 'left',
                            border: selectedId === purchase.id ? '1px solid var(--accent)' : '1px solid var(--border)',
                            borderRadius: 8,
                            background: selectedId === purchase.id ? 'var(--active-bg)' : 'white',
                            padding: 12,
                            cursor: 'pointer',
                            color: 'inherit',
                        }}
                    >
                        <div style={{fontWeight: 600}}>{purchase.code}</div>
                        <div className="text-muted" style={{fontSize: 13}}>
                            {purchase.productName} - {purchase.supplierName}
                        </div>
                        <div className="text-muted" style={{fontSize: 13}}>
                            {purchase.quantity} u - {formatPriceMxn(purchase.totalPrice)}
                        </div>
                    </button>
                ))}
            </div>
        </section>
    )
}


