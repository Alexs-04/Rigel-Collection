import type {AmountFormValues, AmountItem} from '../../types/amounts'
import {formatCurrency} from '../dashboard/formatters'

interface Props {
    amount: AmountItem | null
    isAdmin: boolean
    editing: boolean
    saving: boolean
    form: AmountFormValues
    buyoutPrice: string
    onChangeForm: (key: keyof AmountFormValues, value: string) => void
    onChangeBuyoutPrice: (value: string) => void
    onStartEdit: () => void
    onCancelEdit: () => void
    onSaveEdit: () => void
    onMarkReturned: () => void
    onMarkBoughtOut: () => void
    onDelete: () => void
}

const STATUS_LABEL: Record<string, string> = {
    ACTIVE: 'Activo',
    EXPIRED: 'Vencido',
    RETURNED: 'Devuelto',
    BOUGHT_OUT: 'Comprado',
}

export default function AmountDetailPanel({
    amount,
    isAdmin,
    editing,
    saving,
    form,
    buyoutPrice,
    onChangeForm,
    onChangeBuyoutPrice,
    onStartEdit,
    onCancelEdit,
    onSaveEdit,
    onMarkReturned,
    onMarkBoughtOut,
    onDelete,
}: Props) {
    if (!amount) {
        return (
            <section className="card" style={{padding: 16}}>
                <h2 style={{marginTop: 0, fontSize: 18}}>Detalle del importe</h2>
                <p className="text-muted">Selecciona un importe de la lista para ver su informacion.</p>
            </section>
        )
    }

    return (
        <section className="card" style={{padding: 16}}>
            <h2 style={{marginTop: 0, fontSize: 18}}>Detalle #{amount.folio}</h2>

            {!editing ? (
                <div style={{display: 'grid', gap: 8}}>
                    <p style={{margin: 0}}><strong>Cliente:</strong> {amount.customerName}</p>
                    <p style={{margin: 0}}><strong>Tipo:</strong> {amount.typeLabel}</p>
                    <p style={{margin: 0}}><strong>Estado:</strong> {STATUS_LABEL[amount.status] || amount.status}</p>
                    <p style={{margin: 0}}><strong>Cantidad:</strong> {amount.quantity}</p>
                    <p style={{margin: 0}}><strong>Total:</strong> {formatCurrency(amount.total)}</p>
                    <p style={{margin: 0}}><strong>Vencimiento:</strong> {new Date(amount.expirationDate).toLocaleString('es-MX')}</p>
                    <p style={{margin: 0}}><strong>Registrado por:</strong> {amount.ownerUsername || 'N/A'}</p>
                    {amount.notes ? <p style={{margin: 0}}><strong>Notas:</strong> {amount.notes}</p> : null}
                    {amount.buyoutTotal != null ? <p style={{margin: 0}}><strong>Compra:</strong> {formatCurrency(amount.buyoutTotal)}</p> : null}
                </div>
            ) : (
                <div style={{display: 'grid', gap: 10}}>
                    <div>
                        <label>Cliente</label>
                        <input className="input" value={form.customerName} onChange={(e) => onChangeForm('customerName', e.target.value)} />
                    </div>
                    <div>
                        <label>Descripcion</label>
                        <input className="input" value={form.description} onChange={(e) => onChangeForm('description', e.target.value)} />
                    </div>
                    <div style={{display: 'grid', gridTemplateColumns: 'repeat(3, minmax(0, 1fr))', gap: 8}}>
                        <input className="input" type="number" min={1} value={form.quantity} onChange={(e) => onChangeForm('quantity', e.target.value)} />
                        <input className="input" type="number" min={0} step="0.01" value={form.saleUnitPrice} onChange={(e) => onChangeForm('saleUnitPrice', e.target.value)} />
                        <input className="input" type="number" min={0} step="0.01" value={form.buyoutUnitPrice} onChange={(e) => onChangeForm('buyoutUnitPrice', e.target.value)} />
                    </div>
                    <div>
                        <label>Fecha limite</label>
                        <input className="input" type="datetime-local" value={form.expirationDate} onChange={(e) => onChangeForm('expirationDate', e.target.value)} />
                    </div>
                    <div>
                        <label>Notas</label>
                        <textarea className="input" rows={3} value={form.notes} onChange={(e) => onChangeForm('notes', e.target.value)} />
                    </div>
                </div>
            )}

            <div style={{display: 'flex', gap: 8, flexWrap: 'wrap', marginTop: 12}}>
                {isAdmin && !editing ? (
                    <button type="button" className="btn-primary" onClick={onStartEdit}>Editar</button>
                ) : null}
                {isAdmin && editing ? (
                    <>
                        <button type="button" className="btn-primary" disabled={saving} onClick={onSaveEdit}>Guardar</button>
                        <button type="button" className="btn-ghost" onClick={onCancelEdit}>Cancelar</button>
                    </>
                ) : null}

                {amount.status === 'ACTIVE' ? (
                    <button type="button" className="btn-ghost" disabled={saving} onClick={onMarkReturned}>Marcar devuelto</button>
                ) : null}

                {amount.status === 'EXPIRED' ? (
                    <>
                        <input
                            className="input"
                            style={{maxWidth: 180}}
                            type="number"
                            min={0}
                            step="0.01"
                            placeholder="Precio compra"
                            value={buyoutPrice}
                            onChange={(e) => onChangeBuyoutPrice(e.target.value)}
                        />
                        <button type="button" className="btn-ghost" disabled={saving} onClick={onMarkBoughtOut}>
                            Registrar compra
                        </button>
                    </>
                ) : null}

                {isAdmin ? (
                    <button type="button" className="btn-ghost" disabled={saving} onClick={onDelete}>
                        Eliminar
                    </button>
                ) : null}
            </div>
        </section>
    )
}

