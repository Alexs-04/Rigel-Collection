import type {FormEvent} from 'react'
import type {AmountFormValues, ContainerTypeOption} from '../../types/amounts'

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
        <section className="card" style={{padding: 16}}>
            <h2 style={{marginTop: 0, fontSize: 18}}>Registrar importe</h2>
            <form onSubmit={onSubmit} style={{display: 'grid', gap: 12}}>
                <div style={{display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: 12}}>
                    <div>
                        <label>Cliente</label>
                        <input className="input" value={form.customerName} onChange={(e) => onChange('customerName', e.target.value)} />
                    </div>
                    <div>
                        <label>Tipo de envase</label>
                        <select className="input" value={form.type} onChange={(e) => onChange('type', e.target.value)}>
                            {types.map((item) => (
                                <option key={item.value} value={item.value}>
                                    {item.label}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>

                <div style={{display: 'grid', gridTemplateColumns: 'repeat(3, minmax(0, 1fr))', gap: 12}}>
                    <div>
                        <label>Cantidad</label>
                        <input className="input" type="number" min={1} value={form.quantity}
                               onChange={(e) => onChange('quantity', e.target.value)} />
                    </div>
                    <div>
                        <label>Precio venta (MXN)</label>
                        <input className="input" type="number" min={0} step="0.01" value={form.saleUnitPrice}
                               onChange={(e) => onChange('saleUnitPrice', e.target.value)} />
                    </div>
                    <div>
                        <label>Precio compra sugerido (MXN)</label>
                        <input className="input" type="number" min={0} step="0.01" value={form.buyoutUnitPrice}
                               onChange={(e) => onChange('buyoutUnitPrice', e.target.value)} />
                    </div>
                </div>

                <div style={{display: 'flex', gap: 8, flexWrap: 'wrap'}}>
                    {QUICK_PRICES.map((price) => (
                        <button key={price} type="button" className="btn-ghost" onClick={() => onChange('saleUnitPrice', price)}>
                            MXN ${price}
                        </button>
                    ))}
                </div>

                <div style={{display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: 12}}>
                    <div>
                        <label>Fecha limite</label>
                        <input className="input" type="datetime-local" value={form.expirationDate}
                               onChange={(e) => onChange('expirationDate', e.target.value)} />
                    </div>
                    <div>
                        <label>Descripcion</label>
                        <input className="input" value={form.description}
                               onChange={(e) => onChange('description', e.target.value)} />
                    </div>
                </div>

                <div>
                    <label>Notas</label>
                    <textarea className="input" rows={3} value={form.notes} onChange={(e) => onChange('notes', e.target.value)} />
                </div>

                <button type="submit" className="btn-primary" disabled={saving}>
                    {saving ? 'Guardando...' : 'Registrar importe'}
                </button>

                {message ? <p className="text-muted" style={{margin: 0}}>{message}</p> : null}
            </form>
        </section>
    )
}

