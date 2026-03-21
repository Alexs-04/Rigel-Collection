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
        <section className="card" style={{padding: 16, display: 'grid', gap: 12}}>
            <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                <h2 style={{margin: 0, fontSize: 18}}>Carrito</h2>
                <button className="btn-ghost" type="button" onClick={onClear} disabled={items.length === 0}>
                    Limpiar
                </button>
            </div>

            {items.length === 0 && (
                <p className="text-muted" style={{margin: 0}}>
                    Aun no hay productos en el carrito.
                </p>
            )}

            {items.length > 0 && (
                <div style={{display: 'grid', gap: 10, maxHeight: 380, overflowY: 'auto', paddingRight: 4}}>
                    {items.map((item) => {
                        const lineTotal = Math.max(0, item.quantity * item.unitPrice - item.discount)
                        return (
                            <article
                                key={item.barcode}
                                style={{
                                    border: '1px solid rgba(2, 6, 23, 0.08)',
                                    borderRadius: 10,
                                    padding: 12,
                                    display: 'grid',
                                    gap: 10,
                                }}
                            >
                                <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 12}}>
                                    <div>
                                        <strong>{item.productName}</strong>
                                        <p className="text-muted" style={{margin: 0, fontSize: 12}}>{item.barcode}</p>
                                    </div>
                                    <button className="btn-ghost" type="button" onClick={() => onRemoveItem(item.barcode)}>
                                        Quitar
                                    </button>
                                </div>

                                <div style={{display: 'grid', gridTemplateColumns: 'repeat(3, minmax(0, 1fr))', gap: 8}}>
                                    <label style={{marginBottom: 0}}>
                                        Cantidad
                                        <input
                                            className="input"
                                            type="number"
                                            min={1}
                                            value={item.quantity}
                                            onChange={(event) => onUpdateItem(item.barcode, 'quantity', Number(event.target.value))}
                                        />
                                    </label>
                                    <label style={{marginBottom: 0}}>
                                        Precio unitario
                                        <input
                                            className="input"
                                            type="number"
                                            min={0}
                                            step="0.01"
                                            value={item.unitPrice}
                                            onChange={(event) => onUpdateItem(item.barcode, 'unitPrice', Number(event.target.value))}
                                        />
                                    </label>
                                    <label style={{marginBottom: 0}}>
                                        Descuento
                                        <input
                                            className="input"
                                            type="number"
                                            min={0}
                                            step="0.01"
                                            value={item.discount}
                                            onChange={(event) => onUpdateItem(item.barcode, 'discount', Number(event.target.value))}
                                        />
                                    </label>
                                </div>

                                <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                                    <span className="text-muted" style={{fontSize: 13}}>
                                        {item.quantity} x {formatPriceMxn(item.unitPrice)} - {formatPriceMxn(item.discount)}
                                    </span>
                                    <strong>{formatPriceMxn(lineTotal)}</strong>
                                </div>
                            </article>
                        )
                    })}
                </div>
            )}
        </section>
    )
}

