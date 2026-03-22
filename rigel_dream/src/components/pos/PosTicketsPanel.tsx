import {formatPriceMxn} from '../../utils/productPresentation'
import type {PosTicket} from '../../types/pos'

interface PosTicketsPanelProps {
    tickets: PosTicket[]
    onReload: () => void
    loading: boolean
}

export default function PosTicketsPanel({tickets, onReload, loading}: PosTicketsPanelProps) {
    return (
        <section className="card" style={{padding: 16, display: 'grid', gap: 12}}>
            <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                <h2 style={{margin: 0, fontSize: 18}}>Ventas recientes</h2>
                <button className="btn-ghost" type="button" onClick={onReload} disabled={loading}>
                    Actualizar
                </button>
            </div>

            {tickets.length === 0 && (
                <p className="text-muted" style={{margin: 0}}>
                    Aun no hay tickets registrados.
                </p>
            )}

            {tickets.length > 0 && (
                <div style={{display: 'grid', gap: 10, maxHeight: 380, overflowY: 'auto', paddingRight: 4}}>
                    {tickets.map((ticket) => (
                        <article
                            key={ticket.barcode}
                            style={{
                                border: '1px solid rgba(2, 6, 23, 0.08)',
                                borderRadius: 10,
                                padding: 12,
                                display: 'grid',
                                gap: 8,
                            }}
                        >
                            <div style={{display: 'flex', justifyContent: 'space-between', gap: 8}}>
                                <div>
                                    <strong>{ticket.barcode}</strong>
                                    <p className="text-muted" style={{margin: 0, fontSize: 12}}>
                                        {ticket.consumer} - {ticket.dateAndTime}
                                    </p>
                                </div>
                                <strong>{formatPriceMxn(ticket.totalAmount)}</strong>
                            </div>

                            {ticket.description && (
                                <p className="text-muted" style={{margin: 0, fontSize: 13}}>
                                    {ticket.description}
                                </p>
                            )}

                            <small className="text-muted" style={{fontSize: 12}}>
                                {ticket.products?.length || 0} linea(s) de producto
                            </small>
                        </article>
                    ))}
                </div>
            )}
        </section>
    )
}

