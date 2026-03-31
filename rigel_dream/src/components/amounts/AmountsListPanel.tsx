import type {AmountItem} from '../../types/amounts'
import {formatCurrency} from '../dashboard/formatters'

interface Props {
    loading: boolean
    error: string
    search: string
    onSearchChange: (value: string) => void
    amounts: AmountItem[]
    selectedFolio: number | null
    onSelect: (folio: number) => void
}

const STATUS_LABEL: Record<string, string> = {
    ACTIVE: 'Activo',
    EXPIRED: 'Vencido',
    RETURNED: 'Devuelto',
    BOUGHT_OUT: 'Comprado',
}

export default function AmountsListPanel({
    loading,
    error,
    search,
    onSearchChange,
    amounts,
    selectedFolio,
    onSelect,
}: Props) {
    return (
        <section className="card" style={{padding: 16}}>
            <h2 style={{marginTop: 0, fontSize: 18}}>Importes registrados</h2>
            <input
                className="input"
                placeholder="Buscar por folio, cliente, tipo o estado"
                value={search}
                onChange={(e) => onSearchChange(e.target.value)}
                style={{marginBottom: 12}}
            />

            {loading ? <p className="text-muted">Cargando importes...</p> : null}
            {error ? <p style={{color: '#b91c1c'}}>{error}</p> : null}

            {!loading && !error && amounts.length === 0 ? <p className="text-muted">Sin importes registrados.</p> : null}

            <div style={{display: 'grid', gap: 8}}>
                {amounts.map((item) => {
                    const selected = selectedFolio === item.folio
                    return (
                        <button
                            key={item.folio}
                            type="button"
                            className="btn-ghost"
                            style={{
                                width: '100%',
                                textAlign: 'left',
                                border: `1px solid ${selected ? '#6366f1' : 'rgba(2, 6, 23, 0.08)'}`,
                                background: selected ? '#eef2ff' : '#fff',
                                padding: 12,
                            }}
                            onClick={() => onSelect(item.folio)}
                        >
                            <strong>#{item.folio} - {item.customerName}</strong>
                            <div className="text-muted" style={{fontSize: 13}}>
                                {item.typeLabel} · {STATUS_LABEL[item.status] || item.status} · {formatCurrency(item.total)}
                            </div>
                        </button>
                    )
                })}
            </div>
        </section>
    )
}

