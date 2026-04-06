import {formatPriceMxn} from '../../utils/productPresentation'
import {toPaymentLabel} from '../../utils/paymentPresentation'
import type {PosTicket} from '../../types/pos'

interface PosTicketsPanelProps {
    tickets: PosTicket[]
    onReload: () => void
    loading: boolean
}

export default function PosTicketsPanel({tickets, onReload, loading}: PosTicketsPanelProps) {
    return (
        <section className="ui-card grid gap-3 p-4">
            <div className="flex items-center justify-between">
                <h2 className="m-0 text-lg font-semibold text-slate-900">Ventas recientes</h2>
                <button className="ui-btn-ghost" type="button" onClick={onReload} disabled={loading}>
                    Actualizar
                </button>
            </div>

            {tickets.length === 0 && (
                <p className="m-0 text-sm text-slate-500">
                    Aun no hay tickets registrados.
                </p>
            )}

            {tickets.length > 0 && (
                <div className="grid max-h-[23.75rem] gap-2.5 overflow-y-auto pr-1">
                    {tickets.map((ticket) => (
                        <article
                            key={ticket.barcode}
                            className="grid gap-2 rounded-lg border border-slate-200 p-3"
                        >
                            <div className="flex justify-between gap-2">
                                <div>
                                    <strong className="text-slate-900">{ticket.barcode}</strong>
                                    <p className="m-0 text-xs text-slate-500">
                                        {ticket.consumer} - {ticket.dateAndTime}
                                    </p>
                                </div>
                                <strong className="text-slate-900">{formatPriceMxn(ticket.totalAmount)}</strong>
                            </div>

                            {ticket.description && (
                                <p className="m-0 text-xs text-slate-500">
                                    {ticket.description}
                                </p>
                            )}

                            <small className="text-xs text-slate-500">
                                {ticket.products?.length || 0} linea(s) de producto - Pago: {toPaymentLabel(ticket.payment)}
                            </small>
                        </article>
                    ))}
                </div>
            )}
        </section>
    )
}

