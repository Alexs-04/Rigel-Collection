import {formatPriceMxn} from '../../utils/productPresentation.js'
import type {PurchaseDto} from '../../types/purchases'

interface PurchaseDetailPanelProps {
    purchase: PurchaseDto | null
}

export default function PurchaseDetailPanel({purchase}: PurchaseDetailPanelProps) {
    return (
        <section className="card" style={{padding: 16}}>
            <h2 style={{marginTop: 0, fontSize: 18}}>Detalle de compra</h2>

            {!purchase && (
                <p className="text-muted" style={{marginTop: 0}}>
                    Selecciona una compra para ver su informacion.
                </p>
            )}

            {purchase && (
                <div style={{display: 'grid', gap: 8}}>
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


