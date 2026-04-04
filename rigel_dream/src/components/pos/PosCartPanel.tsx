import {formatPriceMxn} from '../../utils/productPresentation'
import type {PosCartItem} from '../../types/pos'

interface PosCartPanelProps {
    items: PosCartItem[]
    onUpdateItem: (barcode: string, key: 'quantity' | 'unitPrice' | 'discount', value: number) => void
    onRemoveItem: (barcode: string) => void
    onClear: () => void
}

export default function PosCartPanel({
    items,
    onUpdateItem,
    onRemoveItem,
    onClear,
}: PosCartPanelProps) {
    return (
        <section className="ui-card grid gap-3 p-4">
            <div className="flex items-center justify-between">
                <h2 className="m-0 text-lg font-semibold text-slate-900">Carrito</h2>
                <button className="ui-btn-ghost" type="button" onClick={onClear} disabled={items.length === 0}>
                    Limpiar
                </button>
            </div>

            {items.length === 0 && (
                <p className="m-0 text-sm text-slate-500">
                    Aun no hay productos en el carrito.
                </p>
            )}

            {items.length > 0 && (
                <div className="grid max-h-[23.75rem] gap-2.5 overflow-y-auto pr-1">
                    {items.map((item) => {
                        const lineTotal = Math.max(0, item.quantity * item.unitPrice - item.discount)
                        return (
                            <article
                                key={item.barcode}
                                className="grid gap-2.5 rounded-lg border border-slate-200 p-3"
                            >
                                <div className="flex items-center justify-between gap-3">
                                    <div>
                                        <strong className="text-slate-900">{item.productName}</strong>
                                        <p className="m-0 text-xs text-slate-500">{item.barcode}</p>
                                    </div>
                                    <button className="ui-btn-ghost" type="button" onClick={() => onRemoveItem(item.barcode)}>
                                        Quitar
                                    </button>
                                </div>

                                <div className="grid gap-2 sm:grid-cols-3">
                                    <label className="mb-0">
                                        Cantidad
                                        <input
                                            className="ui-input"
                                            type="number"
                                            min={1}
                                            value={item.quantity}
                                            onChange={(event) => onUpdateItem(item.barcode, 'quantity', Number(event.target.value))}
                                        />
                                    </label>
                                    <label className="mb-0">
                                        Precio unitario
                                        <input
                                            className="ui-input"
                                            type="number"
                                            min={0}
                                            step="0.01"
                                            value={item.unitPrice}
                                            onChange={(event) => onUpdateItem(item.barcode, 'unitPrice', Number(event.target.value))}
                                        />
                                    </label>
                                    <label className="mb-0">
                                        Descuento
                                        <input
                                            className="ui-input"
                                            type="number"
                                            min={0}
                                            step="0.01"
                                            value={item.discount}
                                            onChange={(event) => onUpdateItem(item.barcode, 'discount', Number(event.target.value))}
                                        />
                                    </label>
                                </div>

                                <div className="flex items-center justify-between">
                                    <span className="text-xs text-slate-500">
                                        {item.quantity} x {formatPriceMxn(item.unitPrice)} - {formatPriceMxn(item.discount)}
                                    </span>
                                    <strong className="text-slate-900">{formatPriceMxn(lineTotal)}</strong>
                                </div>
                            </article>
                        )
                    })}
                </div>
            )}
        </section>
    )
}

