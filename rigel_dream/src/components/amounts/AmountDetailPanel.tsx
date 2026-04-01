import type {AmountFormValues, AmountItem} from '../../types/amounts'
import {formatCurrency} from '../dashboard/formatters'
import Button from '../ui/Button'
import Card from '../ui/Card'
import {Input} from '../ui/Input'
import {Textarea} from '../ui/Textarea'

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
            <Card className="p-4">
                <h2 className="mb-2 text-lg font-semibold text-slate-900">Detalle del importe</h2>
                <p className="text-sm text-slate-500">Selecciona un importe de la lista para ver su informacion.</p>
            </Card>
        )
    }

    return (
        <Card className="p-4">
            <h2 className="mb-3 text-lg font-semibold text-slate-900">Detalle #{amount.folio}</h2>

            {!editing ? (
                <div className="grid gap-2 text-sm text-slate-700">
                    <p className="m-0"><strong>Cliente:</strong> {amount.customerName}</p>
                    <p className="m-0"><strong>Tipo:</strong> {amount.typeLabel}</p>
                    <p className="m-0"><strong>Estado:</strong> {STATUS_LABEL[amount.status] || amount.status}</p>
                    <p className="m-0"><strong>Cantidad:</strong> {amount.quantity}</p>
                    <p className="m-0"><strong>Total:</strong> {formatCurrency(amount.total)}</p>
                    <p className="m-0"><strong>Vencimiento:</strong> {new Date(amount.expirationDate).toLocaleString('es-MX')}</p>
                    <p className="m-0"><strong>Registrado por:</strong> {amount.ownerUsername || 'N/A'}</p>
                    {amount.notes ? <p className="m-0"><strong>Notas:</strong> {amount.notes}</p> : null}
                    {amount.buyoutTotal != null ? <p className="m-0"><strong>Compra:</strong> {formatCurrency(amount.buyoutTotal)}</p> : null}
                </div>
            ) : (
                <div className="grid gap-3">
                    <div>
                        <label>Cliente</label>
                        <Input value={form.customerName} onChange={(e) => onChangeForm('customerName', e.target.value)} />
                    </div>
                    <div>
                        <label>Descripcion</label>
                        <Input value={form.description} onChange={(e) => onChangeForm('description', e.target.value)} />
                    </div>
                    <div className="grid grid-cols-1 gap-2 lg:grid-cols-3">
                        <Input type="number" min={1} value={form.quantity} onChange={(e) => onChangeForm('quantity', e.target.value)} />
                        <Input type="number" min={0} step="0.01" value={form.saleUnitPrice} onChange={(e) => onChangeForm('saleUnitPrice', e.target.value)} />
                        <Input type="number" min={0} step="0.01" value={form.buyoutUnitPrice} onChange={(e) => onChangeForm('buyoutUnitPrice', e.target.value)} />
                    </div>
                    <div>
                        <label>Fecha limite</label>
                        <Input type="datetime-local" value={form.expirationDate} onChange={(e) => onChangeForm('expirationDate', e.target.value)} />
                    </div>
                    <div>
                        <label>Notas</label>
                        <Textarea rows={3} value={form.notes} onChange={(e) => onChangeForm('notes', e.target.value)} />
                    </div>
                </div>
            )}

            <div className="mt-3 flex flex-wrap gap-2">
                {isAdmin && !editing ? (
                    <Button type="button" onClick={onStartEdit}>Editar</Button>
                ) : null}
                {isAdmin && editing ? (
                    <>
                        <Button type="button" disabled={saving} onClick={onSaveEdit}>Guardar</Button>
                        <Button type="button" variant="ghost" onClick={onCancelEdit}>Cancelar</Button>
                    </>
                ) : null}

                {amount.status === 'ACTIVE' ? (
                    <Button type="button" variant="ghost" disabled={saving} onClick={onMarkReturned}>Marcar devuelto</Button>
                ) : null}

                {amount.status === 'EXPIRED' ? (
                    <>
                        <Input
                            className="max-w-44"
                            type="number"
                            min={0}
                            step="0.01"
                            placeholder="Precio compra"
                            value={buyoutPrice}
                            onChange={(e) => onChangeBuyoutPrice(e.target.value)}
                        />
                        <Button type="button" variant="ghost" disabled={saving} onClick={onMarkBoughtOut}>
                            Registrar compra
                        </Button>
                    </>
                ) : null}

                {isAdmin ? (
                    <Button type="button" variant="danger" disabled={saving} onClick={onDelete}>
                        Eliminar
                    </Button>
                ) : null}
            </div>
        </Card>
    )
}

