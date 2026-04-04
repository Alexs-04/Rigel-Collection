import type {PurchaseFormValues} from '../../types/purchases'
import type {ProductBatch, ProductSupplier, ProductSummary} from '../../types/products'

interface PurchaseFormCardProps {
    form: PurchaseFormValues
    products: ProductSummary[]
    suppliers: ProductSupplier[]
    batches: ProductBatch[]
    saving: boolean
    formMessage: string
    onChange: (key: keyof PurchaseFormValues, value: string | boolean) => void
    onSubmit: (event: React.FormEvent<HTMLFormElement>) => void
}

export default function PurchaseFormCard({
    form,
    products,
    suppliers,
    batches,
    saving,
    formMessage,
    onChange,
    onSubmit,
}: PurchaseFormCardProps) {
    return (
        <section className="ui-card p-5">
            <h2 className="mb-3 mt-0 text-lg font-semibold text-slate-900">Registrar compra</h2>

            <form onSubmit={onSubmit} className="grid gap-2.5">
                <select className="ui-input" value={form.productName} onChange={(e) => onChange('productName', e.target.value)} required>
                    <option value="">Selecciona producto</option>
                    {products.map((product) => (
                        <option key={product.name} value={product.name}>{product.name}</option>
                    ))}
                </select>

                <select className="ui-input" value={form.supplierName} onChange={(e) => onChange('supplierName', e.target.value)} required>
                    <option value="">Selecciona proveedor</option>
                    {suppliers.map((supplier) => (
                        <option key={`${form.productName}-${supplier.name}`} value={supplier.name}>{supplier.name}</option>
                    ))}
                </select>

                <div className="grid gap-2.5 sm:grid-cols-2">
                    <input
                        className="ui-input"
                        type="number"
                        min="1"
                        step="1"
                        placeholder="Cantidad"
                        value={form.quantity}
                        onChange={(e) => onChange('quantity', e.target.value)}
                        required
                    />
                    <input
                        className="ui-input"
                        type="number"
                        min="0"
                        step="0.01"
                        placeholder="Precio unitario"
                        value={form.unitPrice}
                        onChange={(e) => onChange('unitPrice', e.target.value)}
                        required
                    />
                </div>

                <input className="ui-input" type="date" value={form.purchaseDate} onChange={(e) => onChange('purchaseDate', e.target.value)} required />
                <input className="ui-input" placeholder="Notas" value={form.notes} onChange={(e) => onChange('notes', e.target.value)} />

                <label className="mb-0 flex items-center gap-2 text-sm text-slate-600">
                    <input
                        type="checkbox"
                        checked={form.useExistingBatch}
                        onChange={(e) => onChange('useExistingBatch', e.target.checked)}
                    />
                    Usar lote existente
                </label>

                {form.useExistingBatch ? (
                    <select className="ui-input" value={form.batchId} onChange={(e) => onChange('batchId', e.target.value)}>
                        <option value="">Selecciona lote</option>
                        {batches.map((batch) => (
                            <option key={batch.id} value={String(batch.id)}>
                                {batch.code} - Restante: {batch.remainingAmount}
                            </option>
                        ))}
                    </select>
                ) : (
                    <>
                        <input className="ui-input" placeholder="Codigo lote" value={form.batchCode} onChange={(e) => onChange('batchCode', e.target.value)} required />
                        <div className="grid gap-2.5 sm:grid-cols-2">
                            <input className="ui-input" type="date" value={form.receptionDate} onChange={(e) => onChange('receptionDate', e.target.value)} required />
                            <input className="ui-input" type="date" value={form.expirationDate} onChange={(e) => onChange('expirationDate', e.target.value)} required />
                        </div>
                        <input
                            className="ui-input"
                            type="number"
                            min="0"
                            step="0.01"
                            placeholder="Precio lote"
                            value={form.batchPrice}
                            onChange={(e) => onChange('batchPrice', e.target.value)}
                            required
                        />
                    </>
                )}

                <label className="mb-0 flex items-center gap-2 text-sm text-slate-600">
                    <input type="checkbox" checked={form.available} onChange={(e) => onChange('available', e.target.checked)} />
                    Disponible en piso de ventas
                </label>

                <button className="ui-btn-primary" type="submit" disabled={saving || products.length === 0}>
                    {saving ? 'Guardando...' : 'Registrar compra'}
                </button>
            </form>

            {formMessage && <p className="mb-0 text-sm text-slate-500">{formMessage}</p>}
        </section>
    )
}

