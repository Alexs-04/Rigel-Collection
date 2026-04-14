import {formatPriceMxn} from '../../utils/productPresentation.js'
import type {PurchaseDto} from '../../types/purchases'

interface PurchaseDetailPanelProps {
    purchase: PurchaseDto | null
}

export default function PurchaseDetailPanel({purchase}: PurchaseDetailPanelProps) {
    return (
        <section className="ui-card p-4">
            <h2 className="ui-title mt-0 text-lg font-semibold">Detalle de compra</h2>

            {!purchase && (
                <p className="ui-muted mt-0 text-sm">
                    Selecciona una compra para ver su informacion.
                </p>
            )}

            {purchase && (
                <div className="grid gap-2 text-sm text-slate-700 dark:text-slate-200">
                    <div><strong>Codigo:</strong> {purchase.code}</div>
                    <div><strong>Fecha:</strong> {purchase.purchaseDate}</div>
                    <div><strong>Producto:</strong> {purchase.productName}</div>
                    <div><strong>Proveedor:</strong> {purchase.supplierName}</div>
                    <div><strong>Lote:</strong> {purchase.batchCode} (ID {purchase.batchId})</div>
                    <div><strong>Cantidad:</strong> {purchase.quantity}</div>
                    <div><strong>Precio unitario:</strong> {formatPriceMxn(purchase.unitPrice)}</div>
                    <div><strong>Total:</strong> {formatPriceMxn(purchase.totalPrice)}</div>
                    <div><strong>Notas:</strong> {purchase.notes || 'Sin notas'}</div>
                </div>
            )}
        </section>
    )
}


