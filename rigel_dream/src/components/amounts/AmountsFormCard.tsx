import type {FormEvent} from 'react'
import type {AmountFormValues, ContainerTypeOption} from '../../types/amounts'
import Button from '../ui/Button'
import Card from '../ui/Card'
import {Input} from '../ui/Input'
import {Select} from '../ui/Select'
import {Textarea} from '../ui/Textarea'

interface Props {
    form: AmountFormValues
    types: ContainerTypeOption[]
    saving: boolean
    message: string
    onChange: (key: keyof AmountFormValues, value: string) => void
    onSubmit: (event: FormEvent<HTMLFormElement>) => void
}

const QUICK_PRICES = ['5', '10', '15', '100']

export default function AmountsFormCard({form, types, saving, message, onChange, onSubmit}: Props) {
    return (
        <Card className="p-4">
            <h2 className="mb-3 text-lg font-semibold text-slate-900">Registrar importe</h2>
            <form onSubmit={onSubmit} className="grid gap-3">
                <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
                    <div>
                        <label>Cliente</label>
                        <Input value={form.customerName} onChange={(e) => onChange('customerName', e.target.value)} />
                    </div>
                    <div>
                        <label>Tipo de envase</label>
                        <Select value={form.type} onChange={(e) => onChange('type', e.target.value)}>
                            {types.map((item) => (
                                <option key={item.value} value={item.value}>
                                    {item.label}
                                </option>
                            ))}
                        </Select>
                    </div>
                </div>

                <div className="grid grid-cols-1 gap-3 lg:grid-cols-3">
                    <div>
                        <label>Cantidad</label>
                        <Input type="number" min={1} value={form.quantity}
                               onChange={(e) => onChange('quantity', e.target.value)} />
                    </div>
                    <div>
                        <label>Precio venta (MXN)</label>
                        <Input type="number" min={0} step="0.01" value={form.saleUnitPrice}
                               onChange={(e) => onChange('saleUnitPrice', e.target.value)} />
                    </div>
                    <div>
                        <label>Precio compra sugerido (MXN)</label>
                        <Input type="number" min={0} step="0.01" value={form.buyoutUnitPrice}
                               onChange={(e) => onChange('buyoutUnitPrice', e.target.value)} />
                    </div>
                </div>

                <div className="flex flex-wrap gap-2">
                    {QUICK_PRICES.map((price) => (
                        <Button key={price} type="button" variant="ghost" onClick={() => onChange('saleUnitPrice', price)}>
                            MXN ${price}
                        </Button>
                    ))}
                </div>

                <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
                    <div>
                        <label>Fecha limite</label>
                        <Input type="datetime-local" value={form.expirationDate}
                               onChange={(e) => onChange('expirationDate', e.target.value)} />
                    </div>
                    <div>
                        <label>Descripcion</label>
                        <Input value={form.description}
                               onChange={(e) => onChange('description', e.target.value)} />
                    </div>
                </div>

                <div>
                    <label>Notas</label>
                    <Textarea rows={3} value={form.notes} onChange={(e) => onChange('notes', e.target.value)} />
                </div>

                <Button type="submit" disabled={saving}>
                    {saving ? 'Guardando...' : 'Registrar importe'}
                </Button>

                {message ? <p className="m-0 text-sm text-slate-500">{message}</p> : null}
            </form>
        </Card>
    )
}

