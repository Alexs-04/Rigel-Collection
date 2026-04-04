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
        <section className="ui-card grid gap-3 p-4">
            <h2 className="m-0 text-lg font-semibold text-slate-900">Cobro</h2>

            <label className="mb-0">
                Descripcion de venta
                <input
                    className="ui-input"
                    placeholder="Ej. Venta mostrador turno matutino"
                    value={description}
                    onChange={(event) => onDescriptionChange(event.target.value)}
                />
            </label>

            <label className="mb-0">
                Metodo de pago
                <select
                    className="ui-input"
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

            <div className="grid gap-1.5 rounded-lg border border-slate-200 p-3">
                <div className="flex justify-between">
                    <span className="text-sm text-slate-500">Piezas</span>
                    <strong>{totals.items}</strong>
                </div>
                <div className="flex justify-between">
                    <span className="text-sm text-slate-500">Subtotal</span>
                    <strong>{formatPriceMxn(totals.subtotal)}</strong>
                </div>
                <div className="flex justify-between">
                    <span className="text-sm text-slate-500">Descuento</span>
                    <strong>{formatPriceMxn(totals.discount)}</strong>
                </div>
                <div className="flex justify-between text-lg font-semibold text-slate-900">
                    <span>Total</span>
                    <strong>{formatPriceMxn(totals.total)}</strong>
                </div>
            </div>

            {message && (
                <p className={`m-0 text-sm ${message.includes('correctamente') ? 'text-emerald-700' : 'text-red-600'}`}>
                    {message}
                </p>
            )}

            <button className="ui-btn-primary" type="button" onClick={onSubmit} disabled={submitting}>
                {submitting ? 'Procesando...' : 'Cobrar'}
            </button>
        </section>
    )
}

