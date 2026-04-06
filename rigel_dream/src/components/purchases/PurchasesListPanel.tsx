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
            <h2 className="mt-0 text-lg font-semibold text-slate-900">Compras registradas</h2>
            <input
                className="ui-input mb-3"
                placeholder="Buscar por codigo, producto, proveedor o lote"
                value={search}
                onChange={(e) => onSearchChange(e.target.value)}
            />

            {loadingList && <p className="text-sm text-slate-500">Cargando compras...</p>}
            {!loadingList && listError && <p className="text-sm text-slate-500">{listError}</p>}
            {!loadingList && !listError && purchases.length === 0 && <p className="text-sm text-slate-500">No hay compras registradas.</p>}

            <div className="grid gap-2">
                {purchases.map((purchase) => (
                    <button
                        key={purchase.id}
                        type="button"
                        onClick={() => onPurchaseClick(purchase.id)}
                        className={`rounded-lg border p-3 text-left text-inherit transition ${
                            selectedId === purchase.id
                                ? 'border-brand-500 bg-brand-50'
                                : 'border-slate-200 bg-white hover:border-brand-200'
                        }`}
                    >
                        <div className="font-semibold text-slate-900">{purchase.code}</div>
                        <div className="text-xs text-slate-500">
                            {purchase.productName} - {purchase.supplierName}
                        </div>
                        <div className="text-xs text-slate-500">
                            {purchase.quantity} u - {formatPriceMxn(purchase.totalPrice)}
                        </div>
                    </button>
                ))}
            </div>
        </section>
    )
}


