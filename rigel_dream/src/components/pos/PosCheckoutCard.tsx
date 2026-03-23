import {formatPriceMxn} from '../../utils/productPresentation'
import {PAYMENT_OPTIONS_ES} from '../../utils/paymentPresentation'
import type {MethodPayment} from '../../types/pos'

interface PosCheckoutCardProps {
    description: string
    onDescriptionChange: (value: string) => void
    methodPayment: MethodPayment
    onMethodPaymentChange: (value: MethodPayment) => void
    totals: {
        items: number
        subtotal: number
        discount: number
        total: number
    }
    message: string
    submitting: boolean
    onSubmit: () => void
}

export default function PosCheckoutCard({
    description,
    onDescriptionChange,
    methodPayment,
    onMethodPaymentChange,
    totals,
    message,
    submitting,
    onSubmit,
}: PosCheckoutCardProps) {
    return (
        <section className="card" style={{padding: 16, display: 'grid', gap: 12}}>
            <h2 style={{margin: 0, fontSize: 18}}>Cobro</h2>

            <label style={{marginBottom: 0}}>
                Descripcion de venta
                <input
                    className="input"
                    placeholder="Ej. Venta mostrador turno matutino"
                    value={description}
                    onChange={(event) => onDescriptionChange(event.target.value)}
                />
            </label>

            <label style={{marginBottom: 0}}>
                Metodo de pago
                <select
                    className="input"
                    value={methodPayment}
                    onChange={(event) => onMethodPaymentChange(event.target.value as MethodPayment)}
                >
                    {PAYMENT_OPTIONS_ES.map((option) => (
                        <option key={option.value} value={option.value}>
                            {option.label}
                        </option>
                    ))}
                </select>
            </label>

            <div
                style={{
                    border: '1px solid rgba(2, 6, 23, 0.08)',
                    borderRadius: 10,
                    padding: 12,
                    display: 'grid',
                    gap: 6,
                }}
            >
                <div style={{display: 'flex', justifyContent: 'space-between'}}>
                    <span className="text-muted">Piezas</span>
                    <strong>{totals.items}</strong>
                </div>
                <div style={{display: 'flex', justifyContent: 'space-between'}}>
                    <span className="text-muted">Subtotal</span>
                    <strong>{formatPriceMxn(totals.subtotal)}</strong>
                </div>
                <div style={{display: 'flex', justifyContent: 'space-between'}}>
                    <span className="text-muted">Descuento</span>
                    <strong>{formatPriceMxn(totals.discount)}</strong>
                </div>
                <div style={{display: 'flex', justifyContent: 'space-between', fontSize: 18}}>
                    <span>Total</span>
                    <strong>{formatPriceMxn(totals.total)}</strong>
                </div>
            </div>

            {message && (
                <p style={{margin: 0, color: message.includes('correctamente') ? '#15803d' : '#dc2626'}}>
                    {message}
                </p>
            )}

            <button className="btn-primary" type="button" onClick={onSubmit} disabled={submitting}>
                {submitting ? 'Procesando...' : 'Cobrar'}
            </button>
        </section>
    )
}

