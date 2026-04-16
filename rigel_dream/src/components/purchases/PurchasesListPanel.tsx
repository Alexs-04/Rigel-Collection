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
        <section className="ui-card p-4">
            <h2 className="ui-title mt-0 text-lg font-semibold">Compras registradas</h2>
            <input
                className="ui-input mb-3"
                placeholder="Buscar por codigo, producto, proveedor o lote"
                value={search}
                onChange={(e) => onSearchChange(e.target.value)}
            />

            {loadingList && <p className="ui-muted text-sm">Cargando compras...</p>}
            {!loadingList && listError && <p className="ui-muted text-sm">{listError}</p>}
            {!loadingList && !listError && purchases.length === 0 && <p className="ui-muted text-sm">No hay compras registradas.</p>}

            <div className="grid gap-2">
                {purchases.map((purchase) => (
                    <button
                        key={purchase.id}
                        type="button"
                        onClick={() => onPurchaseClick(purchase.id)}
                        className={`ui-list-item ${selectedId === purchase.id ? 'ui-list-item-active' : ''}`}
                    >
                        <div className="ui-title font-semibold">{purchase.code}</div>
                        <div className="ui-muted text-xs">
                            {purchase.productName} - {purchase.supplierName}
                        </div>
                        <div className="ui-muted text-xs">
                            {purchase.quantity} u - {formatPriceMxn(purchase.totalPrice)}
                        </div>
                    </button>
                ))}
            </div>
        </section>
    )
}


